package nl.rijksoverheid.ctr.qrscanner

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import nl.rijksoverheid.ctr.qrscanner.databinding.FragmentScannerBinding
import timber.log.Timber

class QrCodeProcessorImpl: QrCodeProcessor {

    @SuppressLint("UnsafeOptInUsageError")
    override fun process(
        isAdded: Boolean,
        binding: FragmentScannerBinding,
        cameraProvider: ProcessCameraProvider,
        cameraFrame: ImageProxy,
        qrCodeProcessed: (content: String) -> Unit) {
        val reader = MultiFormatReader().apply {
            setHints(mapOf(DecodeHintType.POSSIBLE_FORMATS to arrayListOf(BarcodeFormat.QR_CODE)))
        }

        Handler(Looper.getMainLooper()).post {
            cameraFrame.image?.let { frame ->
                val data = frame.planes[0].buffer.run {
                    rewind()
                    ByteArray(remaining()).also { get(it) }
                }

                val source = PlanarYUVLuminanceSource(
                    data,
                    frame.width,
                    frame.height,
                    0,
                    0,
                    frame.width,
                    frame.height,
                    false
                )
                val bitmap = BinaryBitmap(HybridBinarizer(source))
                try {
                    val result = reader.decodeWithState(bitmap)
                    qrCodeProcessed.invoke(result.text)
                    cameraProvider.unbindAll()
                    if (isAdded) {
                        binding.toolbar.menu.findItem(R.id.flash)
                            .setIcon(R.drawable.ic_torch)
                    }
                } catch (e: NotFoundException) {
                    // try again
                } catch (e: Exception) {
                    Timber.e("Exception while processing frame: $e")
                    throw e
                } finally {
                    reader.reset()
                    cameraFrame.close()
                }
            }
        }
    }
}