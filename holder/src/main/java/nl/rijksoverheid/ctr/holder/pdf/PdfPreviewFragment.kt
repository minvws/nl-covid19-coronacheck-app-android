/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.pdf

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.utils.DialogButtonData
import nl.rijksoverheid.ctr.design.utils.DialogFragmentData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfPreviewBinding
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class PdfPreviewFragment : Fragment(R.layout.fragment_pdf_preview) {

    private val viewModel: PdfPreviewViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfPreviewBinding.bind(view)

        binding.pdfWebView.settings.builtInZoomControls = true
        binding.pdfWebView.settings.displayZoomControls = false
        binding.pdfWebView.settings.allowFileAccess = true

        viewModel.previewLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is PdfPreview.Success -> {
                    binding.pdfWebView.setInitialScale(it.info.initialZoom)
                    binding.pdfWebView.loadDataWithBaseURL(
                        "file:///android_asset/",
                        "<html><body><img src='${it.info.content}' /></body></html>",
                        "text/html",
                        "utf-8",
                        ""
                    )
                }

                is PdfPreview.Error -> {
                    navigateSafety(
                        PdfPreviewFragmentDirections.actionDialog(
                            data = DialogFragmentData(
                                title = R.string.dialog_error_title,
                                message = R.string.general_diskFull_body,
                                positiveButtonData = DialogButtonData.NavigateUp(R.string.ok)
                            )
                        )
                    )
                }
            }
        })

        viewModel.generatePreview(
            screenWidth = resources.displayMetrics.widthPixels,
            filesDir = requireContext().filesDir
        )
    }
}
