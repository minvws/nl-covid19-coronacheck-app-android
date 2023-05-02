/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.data_migration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.IOException
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCase
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationCompressionException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationOtherException
import nl.rijksoverheid.ctr.shared.models.AppErrorResult

abstract class DataMigrationShowQrCodeViewModel : ViewModel() {
    val qrCodesLiveData: LiveData<DataMigrationShowQrViewState> = MutableLiveData()

    abstract fun generateQrCodes(size: Int)
}

class DataMigrationShowQrCodeViewModelImpl(
    private val dataExportUseCase: DataExportUseCase,
    private val qrCodeUseCase: QrCodeUseCase
) : DataMigrationShowQrCodeViewModel() {
    override fun generateQrCodes(size: Int) {
        viewModelScope.launch {
            try {
                val qrCodes = dataExportUseCase.export()
                val bitmaps = qrCodes.map {
                    qrCodeUseCase.qrCode(
                        it.toByteArray(),
                        QrCodeFragmentData.ShouldDisclose.DoNotDisclose,
                        size,
                        size,
                        ErrorCorrectionLevel.M
                    )
                }
                (qrCodesLiveData as MutableLiveData).postValue(
                    DataMigrationShowQrViewState.ShowQrs(
                        bitmaps
                    )
                )
            } catch (exception: Exception) {
                val errorResult = AppErrorResult(
                    e = if (exception is IOException) {
                        DataMigrationCompressionException()
                    } else {
                        DataMigrationOtherException()
                    },
                    step = MigrationHolderStep.Export
                )
                (qrCodesLiveData as MutableLiveData).postValue(
                    DataMigrationShowQrViewState.ShowError(
                        errorResults = listOf(errorResult)
                    )
                )
            }
        }
    }
}
