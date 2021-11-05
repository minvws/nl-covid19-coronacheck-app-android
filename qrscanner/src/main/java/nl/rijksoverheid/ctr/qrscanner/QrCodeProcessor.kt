package nl.rijksoverheid.ctr.qrscanner

import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding

interface QrCodeProcessor {
    fun process(
        isAdded: Boolean,
        binding: FragmentScannerBinding,
        cameraProvider: ProcessCameraProvider,
        cameraFrame: ImageProxy,
        qrCodeProcessed: (content: String) -> Unit)
}