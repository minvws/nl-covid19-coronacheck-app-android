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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import nl.rijksoverheid.ctr.shared.exceptions.AppConfigMissingException
import nl.rijksoverheid.ctr.shared.exceptions.CreatePdfException
import nl.rijksoverheid.ctr.shared.exceptions.NoDccException
import nl.rijksoverheid.ctr.shared.exceptions.StorePdfException
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PdfWebViewModel : ViewModel() {
    val loadingLiveData = MutableLiveData<Event<Boolean>>()
    val errorLiveData = MutableLiveData<Event<Exception>>()
    abstract fun generatePdf(evaluateJavascript: (script: String, valueCallback: ValueCallback<String>?) -> Unit)
    abstract fun storePdf(fileOutputStream: FileOutputStream, contents: String)

    companion object {
        const val pdfMimeType = "data:application/pdf;base64,"
    }
}

class PdfWebViewModelImpl(
    private val filesDirPath: String,
    private val appConfigStorageManager: AppConfigStorageManager,
    private val printExportDccUseCase: PrintExportDccUseCase
) : PdfWebViewModel() {

    override fun generatePdf(evaluateJavascript: (script: String, valueCallback: ValueCallback<String>?) -> Unit) {
        val appConfig = appConfigStorageManager
            .getFileAsBufferedSource(File(filesDirPath, "config.json"))

        if (appConfig == null) {
            errorLiveData.postValue(Event(AppConfigMissingException()))
            return
        }

        viewModelScope.launch {
            val qrs = try {
                printExportDccUseCase.export()
            } catch (exception: Exception) {
                errorLiveData.postValue(Event(NoDccException()))
                return@launch
            }
            try {
                val script = "generatePdf($appConfig, $qrs);"
                evaluateJavascript(script, null)
            } catch (exception: Exception) {
                errorLiveData.postValue(Event(CreatePdfException()))
            }
        }
    }

    override fun storePdf(fileOutputStream: FileOutputStream, contents: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val base64Content = contents.replace("data:application/pdf;base64,", "")
            try {
                val base64DecodedContent = Base64.decode(base64Content, Base64.DEFAULT)
                fileOutputStream.use {
                    it.write(base64DecodedContent)
                    it.flush()
                }
            } catch (exception: Exception) {
                errorLiveData.postValue(Event(StorePdfException()))
            }
            loadingLiveData.postValue(Event(false))
        }
    }
}