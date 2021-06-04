package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.HasOriginUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class YourEventsViewModel : ViewModel() {
    val loading: LiveData<Event<Boolean>> = MutableLiveData()
    val yourEventsResult: LiveData<Event<YourEventsResult>> = MutableLiveData()

    abstract fun saveNegativeTest2(remoteTestResult: RemoteTestResult, rawResponse: ByteArray)
    abstract fun saveVaccinations(remoteEvents: Map<RemoteEventsVaccinations, ByteArray>)
    abstract fun saveNegativeTests3(remoteEvents: Map<RemoteEventsNegativeTests, ByteArray>)
}

class YourEventsViewModelImpl(
    private val saveEventsUseCase: SaveEventsUseCase,
    private val holderDatabaseSyncer: HolderDatabaseSyncer,
    private val hasOriginUseCase: HasOriginUseCase
) : YourEventsViewModel() {

    override fun saveNegativeTest2(negativeTest2: RemoteTestResult, rawResponse: ByteArray) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the event in the database
                saveEventsUseCase.saveNegativeTest2(negativeTest2, rawResponse)

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync()

                // Check if we have origin of type test saved (else something went wrong)
                val hasOrigin = hasOriginUseCase.hasOrigin(OriginType.Test)
                (yourEventsResult as MutableLiveData).value = Event(
                    YourEventsResult(
                        hasOrigin = hasOrigin,
                        databaseSyncerResult = databaseSyncerResult
                    )
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun saveNegativeTests3(remoteEvents: Map<RemoteEventsNegativeTests, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                saveEventsUseCase.saveNegativeTests3(remoteEvents)

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync()

                // Check if we have origin of type test saved (else something went wrong)
                val hasOrigin = hasOriginUseCase.hasOrigin(OriginType.Test)
                (yourEventsResult as MutableLiveData).value = Event(
                    YourEventsResult(
                        hasOrigin = hasOrigin,
                        databaseSyncerResult = databaseSyncerResult
                    )
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }

    override fun saveVaccinations(vaccinations: Map<RemoteEventsVaccinations, ByteArray>) {
        (loading as MutableLiveData).value = Event(true)
        viewModelScope.launch {
            try {
                // Save the events in the database
                saveEventsUseCase.saveVaccinations(vaccinations)

                // Send all events to database and create green cards, origins and credentials
                val databaseSyncerResult = holderDatabaseSyncer.sync()

                // Check if we have origin of type test saved (else something went wrong)
                val hasOrigin = hasOriginUseCase.hasOrigin(OriginType.Test)
                (yourEventsResult as MutableLiveData).value = Event(
                    YourEventsResult(
                        hasOrigin = hasOrigin,
                        databaseSyncerResult = databaseSyncerResult
                    )
                )
            } finally {
                loading.value = Event(false)
            }
        }
    }
}

data class YourEventsResult(val hasOrigin: Boolean, val databaseSyncerResult: DatabaseSyncerResult)
