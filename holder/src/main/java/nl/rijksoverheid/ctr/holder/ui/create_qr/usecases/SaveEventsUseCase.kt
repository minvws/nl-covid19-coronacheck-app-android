package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {
    suspend fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray)
    suspend fun saveRemoteProtocols3(remoteProtocols3: Map<RemoteProtocol3, ByteArray>, eventType: EventType)
}

class SaveEventsUseCaseImpl(private val holderDatabase: HolderDatabase) : SaveEventsUseCase {

    override suspend fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray) {
        // Make remote test results to event group entities to save in the database
        val entity = EventGroupEntity(
            walletId = 1,
            providerIdentifier = negativeTest2.providerIdentifier,
            type = EventType.Test,
            maxIssuedAt = negativeTest2.result?.sampleDate!!,
            jsonData = rawResponse
        )

        // Save entity in database
        holderDatabase.eventGroupDao().insertAll(listOf(entity))
    }


    override suspend fun saveRemoteProtocols3(remoteProtocols3: Map<RemoteProtocol3, ByteArray>, eventType: EventType) {
        val entities = remoteProtocols3.map { remoteProtocol3 ->
            val remoteEvents = remoteProtocol3.key.events
            remoteEvents?.map {
                EventGroupEntity(
                    walletId = 1,
                    providerIdentifier = remoteProtocol3.key.providerIdentifier,
                    type = EventType.Test,
                    maxIssuedAt = getMaxIssuedAt(remoteEvents),
                    jsonData = remoteProtocol3.value
                )
            } ?: listOf()
        }.flatten()

        // Save entity in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }

    private fun getMaxIssuedAt(remoteEvents: List<RemoteEvent>): OffsetDateTime {
        return remoteEvents.map { event -> event.getDate() }.maxByOrNull { date -> date?.toEpochSecond() ?: error("Date should not be null") } ?: error("At least one event must be present with a date")
    }
}
