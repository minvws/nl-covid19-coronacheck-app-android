package nl.rijksoverheid.ctr.qrscanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding
import timber.log.Timber

class QrCodeProcessorImpl: QrCodeProcessor {
    @SuppressLint("UnsafeOptInUsageError")
    override fun process(
        isAdded: Boolean,
        binding: FragmentScannerBinding,
        cameraProvider: ProcessCameraProvider,
        cameraFrame: ImageProxy,
        qrCodeProcessed: (content: String) -> Unit
    ) {
        cameraFrame.image?.let { frame ->
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()

            val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(options)

            val inputImage = InputImage.fromMediaImage(frame, cameraFrame.imageInfo.rotationDegrees)

            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let {
                        qrCodeProcessed.invoke(it)
                        cameraProvider.unbindAll()
                        if (isAdded) {
                            binding.toolbar.menu.findItem(R.id.flash)
                                .setIcon(R.drawable.ic_torch)
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
}