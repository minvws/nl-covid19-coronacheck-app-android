/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.pdf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfExportedBinding
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

class PdfExportedFragment : Fragment(R.layout.fragment_pdf_exported) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfExportedBinding.bind(view)

        binding.previewPdfButton.setOnClickListener {
            navigateSafety(PdfExportedFragmentDirections.actionPdfPreview())
        }

        binding.savePdfButton.setOnClickListener {
            val pdfFile = File(requireContext().filesDir, PdfWebViewFragment.pdfFileName)
            startActivity(
                Intent.createChooser(Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "application/pdf"
                    putExtra(
                        Intent.EXTRA_STREAM,
                        FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().applicationContext.packageName + ".provider",
                            pdfFile
                        )
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }, getString(R.string.holder_pdfExport_success_card_action_save))
            )
        }
    }
}
