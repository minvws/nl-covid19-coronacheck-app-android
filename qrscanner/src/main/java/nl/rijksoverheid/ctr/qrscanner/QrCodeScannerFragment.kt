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
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Size
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.MenuItemCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellManager
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding
import nl.rijksoverheid.ctr.zebrascanner.ZebraManager
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.error.NoBeanDefFoundException
import timber.log.Timber

abstract class QrCodeScannerFragment : Fragment(R.layout.fragment_scanner) {

    private var _binding: FragmentScannerBinding? = null
    val binding get() = _binding!!
    private val zebraManager: ZebraManager? = try {
        get()
    } catch (e: NoBeanDefFoundException) {
        null
    }
    private val honeywellManager: HoneywellManager? = try {
        get()
    } catch (e: NoBeanDefFoundException) {
        null
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    private val qrCodeProcessor: QrCodeProcessor by inject()

    private val dialogUtil: DialogUtil by inject()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (isAdded) {
                if (isCameraPermissionGranted()) {
                    setupScanner(forceCamera = true)
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
        if (zebraManager?.isZebraDevice() == true) {
            // Setup Zebra scanner
            zebraManager.setupZebraScanner(onDatawedgeResultListener = {
                onQrScanned(it)
            })

            binding.laserContainer.visibility = View.VISIBLE
        } else if (honeywellManager?.isHoneywellDevice() == true) {
            // Setup Honeywell scanner
            honeywellManager.setupHoneywellScanner(onDatawedgeResultListener = {
                onQrScanned(it)
            })

            binding.laserContainer.visibility = View.VISIBLE
            binding.laserExplanationFooter.text = getString(R.string.verifier_scanner_honeywell_footer)
        }

        // Set overlay to software accelerated only to fix transparency on certain devices
        binding.overlay.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setCopy()

        // Show header below overlay window after overlay finishes drawing
        binding.overlay.doOnLayout {
            val set = ConstraintSet()
            set.clone(binding.root)
            set.setGuidelineBegin(binding.headerGuideline.id, binding.overlay.bottomOfOverlayWindow)
            set.applyTo(binding.root)
        }

        getCopy().verificationPolicy?.let {
            binding.policyRiskWidget.visibility = View.VISIBLE
            binding.policyText.text = it.title
            binding.policyIndicator.backgroundTintList =
                ColorStateList.valueOf(requireContext().getColor(it.indicatorColor))
        } ?: run { binding.policyRiskWidget.visibility = View.GONE }
    }

    private fun setCopy() {
        val copy = getCopy()
        binding.toolbar.title = copy.title
        binding.scannerFooter.text = copy.message
        binding.scannerFooterButton.text = copy.message

        if (copy.onMessageClicked != null) {
            binding.scannerFooterButton.paintFlags =
                binding.scannerFooterButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            binding.scannerFooterButton.setOnClickListener { copy.onMessageClicked.invoke() }

            // For accessibility there is a separation when the footer is clickable as button or not as text view
            binding.scannerFooterButton.visibility = View.VISIBLE
            binding.scannerFooter.visibility = View.GONE
        } else {
            binding.scannerFooterButton.visibility = View.GONE
            binding.scannerFooter.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onStart() {
        super.onStart()
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val isPhone = resources.configuration.smallestScreenWidthDp < 600
        if (isPortrait || !isPhone) {
            setupScanner()
        }
        if (isPhone) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    protected fun setupScanner(forceCamera: Boolean = false) {
        if (forceCamera || ((zebraManager == null || !zebraManager.isZebraDevice()) && (honeywellManager == null || !honeywellManager.isHoneywellDevice()))) {
            setupCamera()
        } else {
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
        val displaySize = getDisplaySize(previewView.context, requireActivity().windowManager.defaultDisplay)
        val screenAspectRatio = aspectRatio(displaySize.width, displaySize.height)

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

    private fun getDisplaySize(context: Context, display: Display): Size {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            DisplayMetrics().also {
                @Suppress("DEPRECATION")
                display.getRealMetrics(it)
            }.let { Size(it.widthPixels, it.heightPixels) }
        } else {
            val windowContext = context.createWindowContext(display, WindowManager.LayoutParams.TYPE_APPLICATION, null)
            val windowManager = windowContext.getSystemService(WindowManager::class.java)
            val bounds = windowManager.currentWindowMetrics.bounds
            Size(bounds.width(), bounds.height())
        }
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
            cameraExecutor
        ) { cameraFrame ->
            if (isAdded) {
                qrCodeProcessor.process(
                    binding = binding,
                    cameraProvider = cameraProvider,
                    cameraFrame = cameraFrame,
                    qrCodeProcessed = {
                        onQrScanned(it)
                    }
                )
            }
        }

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
        // Teardown Device Manufacturer scanner if one is running
        zebraManager?.teardownZebraScanner()
        honeywellManager?.teardownHoneywellScanner()
    }

    // Handle users swapping between apps meanwhile

    override fun onResume() {
        super.onResume()
        zebraManager?.resumeScanner()
        honeywellManager?.resumeScanner()
    }

    override fun onPause() {
        zebraManager?.suspendScanner()
        honeywellManager?.suspendScanner()
        super.onPause()
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
            dialogUtil.presentDialog(
                context = requireContext(),
                title = rationaleDialog.title,
                message = rationaleDialog.description,
                onDismissCallback = {
                    requestPermission()
                },
                positiveButtonText = rationaleDialog.okayButtonText,
                positiveButtonCallback = {
                    requestPermission()
                }
            )
        }
    }

    data class Copy(
        val title: String,
        val message: String,
        val rationaleDialog: RationaleDialog? = null,
        val onMessageClicked: (() -> Unit)? = null,
        val verificationPolicy: VerificationPolicy? = null
    ) {
        data class RationaleDialog(
            val title: Int,
            val description: String,
            val okayButtonText: Int
        )

        data class VerificationPolicy(
            val title: String,
            @ColorRes val indicatorColor: Int
        )
    }
}
