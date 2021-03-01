/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrscanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import nl.rijksoverheid.ctr.qrscanner.databinding.ActivityScannerBinding
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class QrCodeScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
      
       // Set white status bar icons
        window?.decorView?.systemUiVisibility = 0

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { _, insets ->
            binding.toolbar.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            insets
        }

        // Check for custom message
        intent.extras?.let{
            if(it.containsKey("customMessage")){
                binding.scannerHeader.text = it.getString("customMessage")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupCamera()
    }

    private fun setupCamera() {
        // Set up preview view
        val previewView = binding.previewView

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        // Select camera to use, back facing camera by default
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Request access to CameraX service. Will return a CameraProvider bound to the lifecycle of
        // our activity if one is available
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Retrieve the available CameraProvider and check for permissions
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            if (isCameraPermissionGranted()) {
                // Start setting up our Preview feed and analyzing Usecases
                bindCameraUseCases(cameraProvider, previewView, cameraSelector, screenAspectRatio)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_CAMERA_REQUEST
                )
            }
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindCameraUseCases(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        cameraSelector: CameraSelector,
        aspectRatio: Int
    ) {
        // Unbind exiting Usecases, on some devices these can otherwise persist in the background when
        // reopening the scanner in quick succession. Since the CameraProvider has a limit of 3 usecases this
        // can cause crashes otherwise
        cameraProvider.unbindAll()

        // Bind Usecases for retrieving preview feed from the camera and image processing
        bindPreviewUseCase(cameraProvider, previewView, cameraSelector, aspectRatio)
        bindAnalyseUseCase(cameraProvider, previewView, cameraSelector, aspectRatio)
    }

    /**
     * Set-up camera preview, using the previously selected camera and aspect ratio
     */
    private fun bindPreviewUseCase(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        cameraSelector: CameraSelector,
        aspectRatio: Int
    ) {
        // Set up preview Usecase
        val cameraPreview = Preview.Builder()
            .setTargetAspectRatio(aspectRatio)
            .build()
        cameraPreview.setSurfaceProvider(previewView.surfaceProvider)

        // bind the preview Usecase to the activity's lifecycle so the preview is automatically unbound
        // and disposed whenever the activity closes
        try {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                cameraPreview
            )
        } catch (illegalStateException: IllegalStateException) {
            Timber.e("Camera is currently in an illegal state, either closed or in use by another app: ${illegalStateException.message}")
            throw illegalStateException
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e("Illegal argument, probably too many use cases linked to camera lifecycle, max is three: ${illegalArgumentException.message}")
            throw illegalArgumentException
        }
    }

    /**
     * Set-up analyzer to scan for QR codes only, improving performance.
     * Bound to lifecycle so analyzer is disposed if activity closes
     */
    private fun bindAnalyseUseCase(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
        cameraSelector: CameraSelector,
        aspectRatio: Int
    ) {
        // Set up options for the scanner, limiting it to QR codes only
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

        // Set up the analysis Usecase
        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(aspectRatio)
            .build()

        // Initialize our background executor to process images in the background
        val cameraExecutor = Executors.newSingleThreadExecutor()

        // Add Analyzer to the Usecase, which will receive frames from the camera
        // and processes them using our supplied function
        imageAnalyzer.setAnalyzer(
            cameraExecutor,
            ImageAnalysis.Analyzer { cameraFrame ->
                processCameraFrame(barcodeScanner, cameraFrame)
            }
        )

        // bind the Analyzer Usecase to the activity's lifecycle so the preview is automatically unbound
        // and disposed whenever the activity closes
        try {
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                imageAnalyzer
            )
        } catch (illegalStateException: IllegalStateException) {
            Timber.e("Camera is currently in an illegal state, either closed or in use by another app: ${illegalStateException.message}")
            throw illegalStateException
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e("Illegal argument, probably too many use cases linked to camera lifecycle, max is three: ${illegalArgumentException.message}")
            throw illegalArgumentException
        }
    }

    /**
     * Process frames from CameraX and extract QR codes
     */
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processCameraFrame(
        barcodeScanner: BarcodeScanner,
        cameraFrame: ImageProxy
    ) {
        cameraFrame.image?.let { frame ->
            val inputImage =
                InputImage.fromMediaImage(frame, cameraFrame.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.forEach {
                        Timber.d("Found QR code, contents are ${it.rawValue}")
                        val intent = Intent()
                        intent.putExtra(SCAN_RESULT, it.rawValue)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
                .addOnFailureListener {
                    Timber.e("Exception while processing frame: $it")
                    throw it
                }.addOnCompleteListener {
                    // When the image is from a CameraX analysis use case, we must call .close() on received
                    // images when we're finished using them. Otherwise, new images may not be received or the camera
                    // may stall.
                    cameraFrame.close()
                }
        }

    }

    /**
     *  [androidx.camera.core.ImageAnalysis], [androidx.camera.core.Preview] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                setupCamera()
            } else {
                Timber.e("No camera permission")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val PERMISSION_CAMERA_REQUEST = 1
        const val SCAN_RESULT = "scan_result"

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}