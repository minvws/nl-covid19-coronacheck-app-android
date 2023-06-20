/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.pdf

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.File
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.shared.livedata.Event

abstract class PdfPreviewViewModel : ViewModel() {
    val previewLiveData = MutableLiveData<Event<PdfPreview>>()
    abstract fun generatePreview(screenWidth: Int, filesDir: File)
}

class PdfPreviewViewModelImpl(
    private val previewPdfUseCase: PreviewPdfUseCase
) : PdfPreviewViewModel() {
    override fun generatePreview(screenWidth: Int, filesDir: File) {
        viewModelScope.launch {
            val pdfPreviewResult = previewPdfUseCase.generatePreview(screenWidth, filesDir)
            if (pdfPreviewResult != null) {
                previewLiveData.postValue(Event(PdfPreview.Success(pdfPreviewResult)))
            } else {
                previewLiveData.postValue(Event(PdfPreview.Error))
            }
        }
    }
}
