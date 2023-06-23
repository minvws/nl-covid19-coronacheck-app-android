/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfWebviewBinding
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.shared.exceptions.LoadFileException
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.utils.Accessibility.makeIndeterminateAccessible
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PdfWebViewFragment : BaseFragment(R.layout.fragment_pdf_webview) {

    private val pdfWebViewModel: PdfWebViewModel by viewModel()

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    override fun onButtonClickWithRetryAction() {
        // there is no action to retry in this screen
    }

    override fun getFlow(): Flow {
        return HolderFlow.Pdf
    }

    // local js script, so we are safe
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfWebviewBinding.bind(view)

        binding.loading.makeIndeterminateAccessible(
            context = requireContext(),
            isLoading = true,
            message = R.string.general_loading_description
        )

        pdfWebViewModel.loadingLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.loading.isVisible = it
            navigateSafety(PdfWebViewFragmentDirections.actionPdfExported())
        })

        pdfWebViewModel.errorLiveData.observe(viewLifecycleOwner, EventObserver {
            error(AppErrorResult(HolderStep.PdfExport, it))
        })

        binding.pdfWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(webView: WebView?, url: String?) {
                super.onPageFinished(webView, url)
                webView?.let {
                    pdfWebViewModel.generatePdf(it::evaluateJavascript)
                }
            }
        }
        binding.pdfWebView.addJavascriptInterface(this, "android")
        binding.pdfWebView.settings.allowFileAccess = true
        binding.pdfWebView.settings.javaScriptEnabled = true

        binding.pdfWebView.loadUrl("file:///android_res/raw/print_portal.html")
    }

    @SuppressLint("StringFormatInvalid")
    private fun error(errorResult: ErrorResult) {
        val helpdeskNumber = cachedAppConfigUseCase.getCachedAppConfig().contactInfo.phoneNumber
        presentError(errorResult, getString(
            R.string.holder_pdfExport_error_body,
            helpdeskNumber,
            helpdeskNumber,
            errorCodeStringFactory.get(
                flow = getFlow(),
                errorResults = listOf(errorResult)
            )
        ))
    }

    @JavascriptInterface
    fun onData(value: String) {
        if (value.startsWith(PdfWebViewModel.pdfMimeType)) {
            pdfWebViewModel.storePdf(
                requireContext().openFileOutput(
                    pdfFileName,
                    Context.MODE_PRIVATE
                ), value
            )
        } else {
            val errorResult = AppErrorResult(
                HolderStep.PdfExport,
                LoadFileException()
            )
            error(errorResult)
        }
    }

    companion object {
        const val pdfFileName = "Coronacheck - International.pdf"
    }
}
