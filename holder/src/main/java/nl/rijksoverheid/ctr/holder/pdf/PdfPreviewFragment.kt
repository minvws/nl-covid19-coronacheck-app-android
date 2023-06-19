/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import androidx.fragment.app.Fragment
import java.io.File
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfPreviewBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety

class PdfPreviewFragment : Fragment(R.layout.fragment_pdf_preview) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfPreviewBinding.bind(view)

        try {
            val pdfFile = File(requireContext().filesDir, PdfWebViewFragment.pdfFileName)
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
            binding.pdfImageView.setImageBitmap(pdfBitmap(bitmaps))
        } catch (exception: Exception) {
            findNavControllerSafety()?.popBackStack()
        }
    }

    fun pdfBitmap(bitmaps: List<Bitmap>): Bitmap {
        val width = bitmaps.first().width
        val pageHeight = bitmaps.first().height
        val height = bitmaps.first().height * bitmaps.size
        val comboBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val comboImage = Canvas(comboBitmap)
        bitmaps.forEachIndexed { index, bitmap ->
            comboImage.drawBitmap(bitmap, 0f, (index * pageHeight).toFloat(), null)
        }

        return comboBitmap
    }
}
