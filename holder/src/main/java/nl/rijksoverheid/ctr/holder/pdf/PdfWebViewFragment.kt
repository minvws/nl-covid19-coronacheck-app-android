package nl.rijksoverheid.ctr.holder.pdf

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.JavascriptInterface
import androidx.fragment.app.Fragment
import java.io.File
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.appconfig.usecases.PersistConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPdfWebviewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.scope.scope
import org.koin.androidx.viewmodel.ext.android.viewModel

class PdfWebViewFragment : Fragment(R.layout.fragment_pdf_webview) {

    private val pdfWebViewModel: PdfWebViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentPdfWebviewBinding.bind(view)

//        val localHtmlFilePath = "android.resource://${requireContext().packageName}/${R.raw.index3}"
//        println(localHtmlFilePath)
//        val uri = Uri.parse(localHtmlFilePath)
//        val file = File(uri.toString())
//        println(file.absolutePath)
        binding.pdfWebView.addJavascriptInterface(this, "android")
        binding.pdfWebView.settings.allowFileAccess = true
        binding.pdfWebView.settings.javaScriptEnabled = true

//        resources.openRawResource(R.raw.index3).use {
//            val buffer = ByteArray(it.available())
//            it.read(buffer)
//            val content = String(buffer)
//            binding.pdfWebView.loadData(content, "text/html","utf-8")
//        }
        binding.pdfWebView.loadUrl("file:///android_res/raw/index3.html")
    }

    @JavascriptInterface
    fun onData(value: String) {
        println(value)

        pdfWebViewModel.storePdf(requireContext().openFileOutput("certificate.pdf", Context.MODE_PRIVATE), value)
    }
}
