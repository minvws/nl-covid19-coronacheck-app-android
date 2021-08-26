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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.View
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
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

abstract class QrCodeScannerFragment : Fragment(R.layout.fragment_scanner) {

    private var _binding: FragmentScannerBinding? = null
    val binding get() = _binding!!

    private var zebraIntentSet = false

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (isAdded) {
                if (isCameraPermissionGranted()) {
                    setupCamera()
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

        if( Build.MANUFACTURER == "Zebra Technologies")
            setupImager()

    }

    override fun onStart() {
        super.onStart()
        setupCamera()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onDestroy() {
        super.onDestroy()

        if(( Build.MANUFACTURER == "Zebra Technologies") && (zebraIntentSet)) {
            suspendScanner()
            context?.unregisterReceiver(myDataWedgeBroadcastReceiver)
            context?.unregisterReceiver(myDataWedgeResultBroadcastReceiver)
            zebraIntentSet = false
        }
    }

    private fun setupImager() {

        if (!zebraIntentSet) {

            val filter = IntentFilter()
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            filter.addAction(resources.getString(R.string.activity_intent_filter_action))
            context?.registerReceiver(myDataWedgeBroadcastReceiver, filter)

            val resultFilter = IntentFilter()
            resultFilter.addAction(resources.getString(R.string.datawedge_intent_result_receiver))
            resultFilter.addCategory("android.intent.category.DEFAULT")
            context?.registerReceiver(myDataWedgeResultBroadcastReceiver, resultFilter)

            zebraIntentSet = true

            setupDataWedgeProfile()

        }
    }

    private val myDataWedgeBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            intent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))?.let {
                suspendScanner()
                onQrScanned(it)
            }
        }
    }

    private val myDataWedgeResultBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            intent.getStringArrayExtra(resources.getString(R.string.datawedge_intent_result_get_profile_list))?.let {

                var profileExist = false

                if (it.isNotEmpty()) {
                    it.forEach { profile ->
                        if(profile.equals(resources.getString(R.string.datawedge_profile_name)))
                            profileExist = true
                    }
                }
                if (!profileExist) {
                    createDataWedgeProfile()
                }
            }
        }
    }

    private fun setupDataWedgeProfile() {
        val i = Intent()
        i.action = resources.getString(R.string.datawedge_intent_api_ACTION)
        i.putExtra(resources.getString(R.string.datawedge_intent_get_profile_list), "")
        context?.sendBroadcast(i)
    }

    private fun createDataWedgeProfile() {

        val setConfigBundle = Bundle()
        setConfigBundle.putString("PROFILE_NAME", resources.getString(R.string.datawedge_profile_name))
        setConfigBundle.putString("PROFILE_ENABLED", "true")
        setConfigBundle.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST")
        setConfigBundle.putString("RESET_CONFIG", "false")

        // Associate profile with this app
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", context?.packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        setConfigBundle.putParcelableArray("APP_LIST", arrayOf(appConfig))
        setConfigBundle.remove("PLUGIN_CONFIG")

        // Configure scanner input parameters, decoders for barcode type to be sent to this app
        val barcodeInputConfig = Bundle()
        barcodeInputConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeInputConfig.putString("RESET_CONFIG", "true")
        val barcodeInputProps = Bundle()
        barcodeInputProps.putString("scanner_input_enabled","true")
        barcodeInputProps.putString("decoder_qrcode","true")
        barcodeInputConfig.putBundle("PARAM_LIST",barcodeInputProps)

        // Configure keystroke output for captured data to be sent to this app
        val keystrokeConfig = Bundle()
        keystrokeConfig.putString("PLUGIN_NAME", "KEYSTROKE")
        keystrokeConfig.putString("RESET_CONFIG", "true")
        val keystrokeProps = Bundle()
        keystrokeProps.putString("keystroke_output_enabled","false")
        keystrokeConfig.putBundle("PARAM_LIST",keystrokeProps)

        // Configure intent output for captured data to be sent to this app
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", resources.getString(R.string.activity_intent_filter_action))
        intentProps.putString("intent_category", "android.intent.category.DEFAULT")
        intentProps.putString("intent_delivery", "2")
        intentConfig.putBundle("PARAM_LIST", intentProps)

        // Add configurations into a collection
        val configBundles: ArrayList<Parcelable> = ArrayList()
        configBundles.add(barcodeInputConfig)
        configBundles.add(keystrokeConfig)
        configBundles.add(intentConfig)
        setConfigBundle.putParcelableArrayList("PLUGIN_CONFIG", configBundles)

        // Broadcast the intent
        val intent = Intent()
        intent.action = resources.getString(R.string.datawedge_intent_api_ACTION)
        intent.putExtra("com.symbol.datawedge.api.SET_CONFIG", setConfigBundle)
        context?.sendBroadcast(intent)
    }

    private fun suspendScanner() {
        val intent = Intent()
        intent.action = resources.getString(R.string.datawedge_intent_api_ACTION)
        intent.putExtra(resources.getString(R.string.datawedge_intent_api_SCANNER_INPUT_PLUGIN), "SUSPEND_PLUGIN")
        intent.putExtra("SEND_RESULT", "true")
        intent.putExtra("COMMAND_IDENTIFIER", "MY_SUSPEND_SCANNER") //Unique identifier
        context?.sendBroadcast(intent)
    }

    private fun resumeScanner() {
        val intent = Intent()
        intent.action = resources.getString(R.string.datawedge_intent_api_ACTION)
        intent.putExtra(resources.getString(R.string.datawedge_intent_api_SCANNER_INPUT_PLUGIN), "RESUME_PLUGIN")
        intent.putExtra("SEND_RESULT", "true")
        intent.putExtra("COMMAND_IDENTIFIER", "MY_RESUME_SCANNER") //Unique identifier
        context?.sendBroadcast(intent)
    }

    protected fun setupCamera() {

        resumeScanner()

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
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

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
                viewLifecycleOwner,
                cameraSelector,
                cameraPreview
            ).also { camera ->
                // If device supports flash, enable flash functionality
                if (camera.cameraInfo.hasFlashUnit()) {
                    binding.toolbar.menu.findItem(R.id.flash).isVisible = true
                    binding.toolbar.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.flash -> {
                                if (camera.cameraInfo.torchState.value != TorchState.ON) {
                                    camera.cameraControl.enableTorch(true)
                                    MenuItemCompat.setContentDescription(
                                        item,
                                        resources.getString(R.string.accessibility_flash_off)
                                    )
                                    item.setIcon(R.drawable.ic_flash_off)
                                } else {
                                    camera.cameraControl.enableTorch(false)
                                    MenuItemCompat.setContentDescription(
                                        item,
                                        resources.getString(R.string.accessibility_flash_on)
                                    )
                                    item.setIcon(R.drawable.ic_flash_on)
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
            .setBarcodeFormats(getBarcodeFormats().first(), *getBarcodeFormats().toIntArray())
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
            { cameraFrame ->
                processCameraFrame(cameraProvider, barcodeScanner, cameraFrame)
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
            throw illegalArgumentException
        }
    }

    /**
     * Process frames from CameraX and extract QR codes
     */
    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun processCameraFrame(
        cameraProvider: ProcessCameraProvider,
        barcodeScanner: BarcodeScanner,
        cameraFrame: ImageProxy
    ) {
        cameraFrame.image?.let { frame ->
            val inputImage =
                InputImage.fromMediaImage(frame, cameraFrame.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let {
                        onQrScanned(it)
                        cameraProvider.unbindAll()
                        if (isAdded) {
                            binding.toolbar.menu.findItem(R.id.flash).setIcon(R.drawable.ic_flash_on)
                        }
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

    abstract fun onQrScanned(content: String)
    abstract fun getCopy(): Copy
    abstract fun getBarcodeFormats(): List<Int>

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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
