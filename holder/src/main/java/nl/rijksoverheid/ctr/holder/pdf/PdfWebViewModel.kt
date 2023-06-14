/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.pdf

import android.util.Base64
import android.webkit.ValueCallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import timber.log.Timber

abstract class PdfWebViewModel: ViewModel() {
        abstract fun generatePdf(evaluateJavascript: (script: String, valueCallback: ValueCallback<String>) -> Unit)
    abstract fun storePdf(fileOutputStream: FileOutputStream, contents: String)

    companion object {
        const val pdfMimeType = "data:application/pdf;base64,"
    }
}

class PdfWebViewModelImpl(
    private val filesDirPath: String,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val printExportDccUseCase: PrintExportDccUseCase
): PdfWebViewModel(), ValueCallback<String> {

    override fun onReceiveValue(value: String?) {
        if (value == null || value.startsWith(pdfMimeType)) return
        viewModelScope.launch(Dispatchers.IO) {
            val base64Content = value.replace(pdfMimeType, "")
            val base64DecodedContent = Base64.decode(base64Content, Base64.DEFAULT)
            val file = File(filesDirPath, "exportedCertificates.pdf")
            file.outputStream().use {
                it.write(base64DecodedContent)
                it.flush()
            }
        }
    }

    override fun generatePdf(evaluateJavascript: (script: String, valueCallback: ValueCallback<String>) -> Unit) {
        val appConfig = appConfigStorageManager
            .getFileAsBufferedSource(File(filesDirPath, "config.json"))

        if (appConfig == null) {
            return
        }

        viewModelScope.launch {
            val qrs = printExportDccUseCase.export()
            val script = "generatePdf($appConfig, $qrs);"
            evaluateJavascript(script, this@PdfWebViewModelImpl)
        }
    }

    override fun storePdf(fileOutputStream: FileOutputStream, contents: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val base64Content = contents.replace("data:application/pdf;base64,", "")
            val base64DecodedContent = Base64.decode(base64Content, Base64.DEFAULT)
            fileOutputStream.use {
                it.write(base64DecodedContent)
                it.flush()
            }
        }
    }
}
