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
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.livedata.Event

data class ProgressBarState(val progress: Int, val max: Int) {
    fun calculateProgressPercentage(): Int {
        return ((progress.toFloat() / max.toFloat()) * 100).toInt()
    }
}

abstract class DataMigrationScanQrViewModel : ViewModel() {
    val progressBarLiveData: LiveData<ProgressBarState> = MutableLiveData()
    val scanFinishedLiveData: LiveData<Event<YourEventsFragmentType.RemoteProtocol3Type>> =
        MutableLiveData()

    abstract fun onQrScanned(content: String)
}

class DataMigrationScanQrViewModelImpl(
    private val dataMigrationImportUseCase: DataMigrationImportUseCase,
    private val dataMigrationPayloadUseCase: DataMigrationPayloadUseCase,
    private val configProvidersUseCase: ConfigProvidersUseCase
) : DataMigrationScanQrViewModel() {

    private val scannedChunks = mutableListOf<MigrationParcel>()

    override fun onQrScanned(content: String) {
        viewModelScope.launch {
            val migrationParcel = dataMigrationImportUseCase.import(content)

            if (migrationParcel != null) {
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

                if (scannedChunks.size == migrationParcel.numberOfPackages) {
                    val eventGroupParcels = dataMigrationImportUseCase.merge(scannedChunks)
                    val remoteEventsMap = eventGroupParcels.mapNotNull {
                        val remoteProtocol = dataMigrationPayloadUseCase.parsePayload(it.jsonData)

                        if (remoteProtocol != null) {
                            mapOf(remoteProtocol to it.jsonData)
                        } else {
                            null
                        }
                    }
                        .fold(mapOf<RemoteProtocol, ByteArray>()) { protocol, byteArray -> protocol + byteArray }

                    val eventProvidersResult = configProvidersUseCase.eventProviders()

                    if (eventProvidersResult is EventProvidersResult.Success) {
                        (scanFinishedLiveData as MutableLiveData).postValue(Event(
                            YourEventsFragmentType.RemoteProtocol3Type(
                                remoteEvents = remoteEventsMap,
                                eventProviders = eventProvidersResult.eventProviders.map {
                                    EventProvider(
                                        it.providerIdentifier,
                                        it.name
                                    )
                                }
                            )))
                    } else {
                        println("Event providers error: $eventProvidersResult")
                    }
                }
            }
        }
    }
}
