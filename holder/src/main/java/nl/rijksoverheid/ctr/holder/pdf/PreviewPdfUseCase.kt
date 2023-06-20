package nl.rijksoverheid.ctr.holder.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PreviewPdfUseCase {
    suspend fun generatePreview(screenWidth: Int, filesDir: File): PDfPreviewInfo?
}

class PreviewPdfUseCaseImpl : PreviewPdfUseCase {
    override suspend fun generatePreview(screenWidth: Int, filesDir: File): PDfPreviewInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val pdfFile = File(filesDir, PdfWebViewFragment.pdfFileName)
                val parcelFileDescriptor =
                    ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                val bitmaps = mutableListOf<Bitmap>()
                for (i in 0 until pdfRenderer.pageCount) {
                    val currentPage = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(
                        currentPage.width,
                        currentPage.height,
                        Bitmap.Config.ARGB_8888
                    )
                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    currentPage.close()
                }
                pdfRenderer.close()

                val bitmapWidth = bitmaps.first().width
                val initialZoom = ((screenWidth.toFloat() / bitmapWidth.toFloat()) * 100).toInt()

                PDfPreviewInfo(
                    content = pdfBitmap(bitmaps),
                    initialZoom = initialZoom
                )
            } catch (exception: Exception) {
                null
            }
        }
    }

    private fun pdfBitmap(bitmaps: List<Bitmap>): String {
        val width = bitmaps.first().width
        val pageHeight = bitmaps.first().height
        val height = bitmaps.first().height * bitmaps.size
        val comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val comboImage = Canvas(comboBitmap)
        bitmaps.forEachIndexed { index, bitmap ->
            comboImage.drawBitmap(bitmap, 0f, (index * pageHeight).toFloat(), null)
        }

        val byteArrayOutputStream = ByteArrayOutputStream()
        comboBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return "data:image/png;base64,${Base64.encodeToString(byteArray, Base64.DEFAULT)}"
    }
}
