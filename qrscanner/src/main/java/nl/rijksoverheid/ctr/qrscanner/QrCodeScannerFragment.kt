/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrscanner

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding
import nl.rijksoverheid.ctr.zebrascanner.ZebraManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.error.NoBeanDefFoundException
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class QrCodeScannerFragment : Fragment(R.layout.fragment_scanner) {

    private var _binding: FragmentScannerBinding? = null
    val binding get() = _binding!!
    private val zebraManager: ZebraManager? = try {
        get()
    } catch (e: NoBeanDefFoundException) {
        null
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
    
    private val qrCodeProcessor: QrCodeProcessor by inject()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (isAdded) {
                if (isCameraPermissionGranted()) {
                    setUpScanner(forceCamera = true)
                } else {
                    val rationaleDialog = getCopy().rationaleDialog
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) && rationaleDialog != null) {
                        showRationaleDialog(rationaleDialog)
                    }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentScannerBinding.bind(view)
        if(zebraManager?.isZebraDevice() == true){
            // Setup Zebra scanner
            zebraManager.setupZebraScanner(onDatawedgeResultListener = {
                onQrScanned(it)
            })

            binding.zebraContrainer.visibility = View.VISIBLE
        }

        // Set overlay to software accelerated only to fix transparency on certain devices
        binding.overlay.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            binding.toolbar.setPadding(0, insets.systemWindowInsetTop, 0, 0)
            (binding.scannerFooterContainer.layoutParams as ConstraintLayout.LayoutParams).updateMargins(
                bottom = insets.systemWindowInsetBottom
            )
            insets
        }

        binding.scannerFooter.run {
            text = getCopy().message
            getCopy().onMessageClicked?.let { onClicked ->
                paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                setOnClickListener { onClicked.invoke() }
            }
        }
        binding.toolbar.title = getCopy().title

        // Show header below overlay window after overlay finishes drawing
        binding.overlay.doOnLayout {
            val set = ConstraintSet()
            set.clone(binding.root)
            set.setGuidelineBegin(binding.headerGuideline.id, binding.overlay.bottomOfOverlayWindow)
            set.applyTo(binding.root)
        }
    }

    override fun onStart() {
        super.onStart()
        setUpScanner()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    protected fun setUpScanner(forceCamera: Boolean = false) {
        if (forceCamera || zebraManager == null || zebraManager.isZebraDevice() == false) {
            setupCamera()
        } else {
            // Enable Zebra scanners
            zebraManager.resumeScanner()

            binding.toolbar.menu.findItem(R.id.camera).isVisible = true
            binding.toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.camera -> {
                        setupCamera()
                        item.isVisible = false
                    }
                }
                true
            }
        }
    }

    private fun setupCamera(lensFacing: Int = CameraSelector.LENS_FACING_BACK) {
        // make sure it's still added when coming back from a dialog
        if (!isAdded) {
            return
        }

        // Set up preview view
        val previewView = binding.previewView

        // Get screen metrics used to setup camera for full screen resolution
        val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)

        // Select camera to use, back facing camera by default
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Request access to CameraX service. Will return a CameraProvider bound to the lifecycle of
        // our activity if one is available
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Retrieve the available CameraProvider and check for permissions
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            if (isAdded && isCameraPermissionGranted()) {
                // Start setting up our Preview feed and analyzing Usecases
                bindCameraUseCases(cameraProvider, previewView, cameraSelector, screenAspectRatio)
            } else {
                requestPermission()
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun requestPermission() {
        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )
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

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(aspectRatio)
            .build()

        // Bind Usecases for retrieving preview feed from the camera and image processing
        bindPreviewUseCase(cameraProvider, previewView, cameraSelector, aspectRatio)
        bindAnalyseUseCase(cameraProvider, cameraSelector, aspectRatio)
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
                viewLifecycleOwner,
                cameraSelector,
                cameraPreview
            ).also { camera ->
                try {
                    previewView.focusOnTouch(camera.cameraControl)
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
                // If device supports flash, enable flash functionality
                if (camera.cameraInfo.hasFlashUnit()) {
                    binding.toolbar.menu.findItem(R.id.flash)?.let { flashItem ->
                        flashItem.isVisible = true
                        MenuItemCompat.setContentDescription(
                            flashItem,
                            resources.getString(R.string.accessibility_flash_on)
                        )
                    }

                    binding.toolbar.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.flash -> {
                                if (camera.cameraInfo.torchState.value != TorchState.ON) {
                                    camera.cameraControl.enableTorch(true)
                                    MenuItemCompat.setContentDescription(
                                        item,
                                        resources.getString(R.string.accessibility_flash_off)
                                    )
                                } else {
                                    camera.cameraControl.enableTorch(false)
                                    MenuItemCompat.setContentDescription(
                                        item,
                                        resources.getString(R.string.accessibility_flash_on)
                                    )
                                }
                            }
                        }
                        true
                    }
                }
            }
        } catch (illegalStateException: IllegalStateException) {
            Timber.e("Camera is currently in an illegal state, either closed or in use by another app: ${illegalStateException.message}")
            throw illegalStateException
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e("Illegal argument, probably too many use cases linked to camera lifecycle, max is three: ${illegalArgumentException.message}")
            // back camera failed to be used, try also the front one; it
            // seems that some scanner devices report it instead
            setupCamera(CameraSelector.LENS_FACING_FRONT)
        }
    }

    /**
     * Set-up analyzer to scan for QR codes only, improving performance.
     * Bound to lifecycle so analyzer is disposed if activity closes
     */
    private fun bindAnalyseUseCase(
        cameraProvider: ProcessCameraProvider,
        cameraSelector: CameraSelector,
        aspectRatio: Int
    ) {
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
            { cameraFrame ->
                qrCodeProcessor.process(
                    isAdded = isAdded,
                    binding = binding,
                    cameraProvider = cameraProvider,
                    cameraFrame = cameraFrame,
                    qrCodeProcessed = {
                        onQrScanned(it)
                    }
                )
            }
        )

        // bind the Analyzer Usecase to the activity's lifecycle so the preview is automatically unbound
        // and disposed whenever the activity closes
        try {
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                imageAnalyzer
            )
        } catch (illegalStateException: IllegalStateException) {
            Timber.e("Camera is currently in an illegal state, either closed or in use by another app: ${illegalStateException.message}")
            throw illegalStateException
        } catch (illegalArgumentException: IllegalArgumentException) {
            Timber.e("Illegal argument, probably too many use cases linked to camera lifecycle, max is three: ${illegalArgumentException.message}")
            setupCamera(CameraSelector.LENS_FACING_FRONT)
        }
    }

    abstract fun onQrScanned(content: String)
    abstract fun getCopy(): Copy

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        // Teardown Zebra scanner if one is running
        zebraManager?.teardownZebraScanner()
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

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showRationaleDialog(rationaleDialog: Copy.RationaleDialog) {
        if (isAdded) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(rationaleDialog.title)
                .setMessage(rationaleDialog.description)
                .setPositiveButton(rationaleDialog.okayButtonText) { dialog, which ->
                    requestPermission()
                }
                .show()
        }
    }

    data class Copy(
        val title: String,
        val message: String,
        val rationaleDialog: RationaleDialog? = null,
        val onMessageClicked: (() -> Unit)? = null
    ) {
        data class RationaleDialog(
            val title: String,
            val description: String,
            val okayButtonText: String
        )
    }
}
