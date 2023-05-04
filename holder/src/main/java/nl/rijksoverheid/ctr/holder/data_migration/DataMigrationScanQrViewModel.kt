/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.data_migration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.usecases.ConfigProvidersUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.EventProvidersResult
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationDecodingErrorException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationInvalidNumberOfPackagesException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationInvalidVersionException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationOtherException
import nl.rijksoverheid.ctr.shared.exceptions.NoProvidersException
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.AppErrorResult

data class ProgressBarState(val progress: Int, val max: Int) {
    fun calculateProgressPercentage(): Int {
        return ((progress.toFloat() / max.toFloat()) * 100).toInt()
    }
}

sealed class DataMigrationScanQrState {
    data class Success(val type: YourEventsFragmentType.RemoteProtocol3Type) :
        DataMigrationScanQrState()

    data class Error(val errorResult: AppErrorResult) : DataMigrationScanQrState()
}

abstract class DataMigrationScanQrViewModel : ViewModel() {
    val progressBarLiveData: LiveData<ProgressBarState> = MutableLiveData()
    val scanFinishedLiveData: LiveData<Event<DataMigrationScanQrState>> =
        MutableLiveData()

    abstract fun onQrScanned(content: String)
}

class DataMigrationScanQrViewModelImpl(
    private val dataMigrationImportUseCase: DataMigrationImportUseCase,
    private val dataMigrationPayloadUseCase: DataMigrationPayloadUseCase,
    private val configProvidersUseCase: ConfigProvidersUseCase
) : DataMigrationScanQrViewModel() {

    private val scannedChunks = mutableListOf<MigrationParcel>()

    private fun scanFinished(state: DataMigrationScanQrState) {
        (scanFinishedLiveData as MutableLiveData).postValue(Event(state))
    }

    private fun scanFinishedWithError(exception: Exception) {
        scanFinished(
            DataMigrationScanQrState.Error(
                AppErrorResult(
                    HolderStep.DataMigrationImport,
                    exception
                )
            )
        )
    }

    override fun onQrScanned(content: String) {
        viewModelScope.launch {
            val migrationParcel = try {
                dataMigrationImportUseCase.import(content)
            } catch (exception: Exception) {
                scanFinishedWithError(DataMigrationDecodingErrorException())
                null
            }

            when {
                migrationParcel == null -> {
                    scanFinishedWithError(DataMigrationOtherException())
                }
                migrationParcel.version != DataExportUseCaseImpl.version -> {
                    scanFinishedWithError(DataMigrationInvalidVersionException())
                }
                else -> {
                    next(migrationParcel)
                }
            }
        }
    }

    private suspend fun next(migrationParcel: MigrationParcel) {
        when {
            scannedChunks.size > migrationParcel.numberOfPackages -> {
                scanFinishedWithError(DataMigrationInvalidNumberOfPackagesException())
            }
            scannedChunks.size == migrationParcel.numberOfPackages -> {
                updateProgress(migrationParcel)
                val eventGroupParcels = try {
                    dataMigrationImportUseCase.merge(scannedChunks)
                } catch (exception: Exception) {
                    scanFinishedWithError(DataMigrationDecodingErrorException())
                    null
                }
                if (eventGroupParcels != null) {
                    val remoteEventsMap = eventGroupParcels.mapNotNull {
                        val remoteProtocol =
                            dataMigrationPayloadUseCase.parsePayload(it.jsonData)

                        if (remoteProtocol != null) {
                            mapOf(remoteProtocol to it.jsonData)
                        } else {
                            null
                        }
                    }
                        .fold(mapOf<RemoteProtocol, ByteArray>()) { protocol, byteArray -> protocol + byteArray }

                    val eventProvidersResult = configProvidersUseCase.eventProviders()

                    if (eventProvidersResult is EventProvidersResult.Success) {
                        scanFinished(
                            state = DataMigrationScanQrState.Success(
                                type = YourEventsFragmentType.RemoteProtocol3Type(
                                    remoteEvents = remoteEventsMap,
                                    eventProviders = eventProvidersResult.eventProviders.map {
                                        EventProvider(
                                            it.providerIdentifier,
                                            it.name
                                        )
                                    }
                                )
                            )
                        )
                    } else {
                        scanFinishedWithError(NoProvidersException.Migration)
                    }
                }
            }
            else -> {
                updateProgress(migrationParcel)
            }
        }
    }
    private fun updateProgress(migrationParcel: MigrationParcel) {
        val currentState = progressBarLiveData.value
        val currentProgress = currentState?.progress ?: 0
        if (!scannedChunks.map { it.payload }.contains(migrationParcel.payload)) {
            scannedChunks.add(migrationParcel)
            (progressBarLiveData as MutableLiveData).postValue(
                ProgressBarState(
                    progress = currentProgress + 1,
                    max = migrationParcel.numberOfPackages
                )
            )
        }
    }
}
