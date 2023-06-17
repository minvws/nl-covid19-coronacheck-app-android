/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import androidx.fragment.app.Fragment
import java.io.File
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfPreviewBinding

class PdfPreviewFragment : Fragment(R.layout.fragment_pdf_preview) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfPreviewBinding.bind(view)

        try {
            val pdfFile = File(requireContext().filesDir, PdfWebViewFragment.pdfFileName)
            val parcelFileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val currentPage = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(
                currentPage.width,
                currentPage.height,
                Bitmap.Config.ARGB_8888
            )
            currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            binding.pdfImageView.setImageBitmap(bitmap)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
