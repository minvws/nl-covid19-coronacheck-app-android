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
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfWebviewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PdfWebViewFragment : Fragment(R.layout.fragment_pdf_webview) {

    private val pdfWebViewModel: PdfWebViewModel by viewModel()

    // local js script, so we are safe
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfWebviewBinding.bind(view)

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

    @JavascriptInterface
    fun onData(value: String) {
        if (value.startsWith(PdfWebViewModel.pdfMimeType)) {
            pdfWebViewModel.storePdf(
                requireContext().openFileOutput(
                    "certificate.pdf",
                    Context.MODE_PRIVATE
                ), value
            )
        }
    }
}
