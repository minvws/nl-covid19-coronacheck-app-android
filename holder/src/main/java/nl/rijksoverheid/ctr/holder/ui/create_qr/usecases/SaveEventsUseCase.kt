package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {
    suspend fun save(remoteEvents: Map<RemoteEvents, ByteArray>)
    suspend fun saveRemoteEventsNegativeTests(remoteEventsNegativeTests: Map<RemoteEventsNegativeTests, ByteArray>)
    suspend fun save(remoteTestResult: RemoteTestResult, rawResponse: ByteArray)
}

class SaveEventsUseCaseImpl(private val holderDatabase: HolderDatabase) : SaveEventsUseCase {

    override suspend fun save(remoteEvents: Map<RemoteEvents, ByteArray>) {
        // Map remote events to EventGroupEntity to save in the database
        val entities = remoteEvents.map {
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = it.key.providerIdentifier,
                type = EventType.Vaccination,
                maxIssuedAt = it.key.events.map { event -> event.getDate() }
                    .maxByOrNull { date -> date.toEpochDay() }
                    ?.atStartOfDay()?.atOffset(
                        ZoneOffset.UTC
                    )!!,
                jsonData = it.value
            )
        }

        // Save entities in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }

    override suspend fun save(remoteTestResult: RemoteTestResult, rawResponse: ByteArray) {
        // Make remote test results to event group entities to save in the database
        val entity = EventGroupEntity(
            walletId = 1,
            providerIdentifier = remoteTestResult.providerIdentifier,
            type = EventType.Test,
            maxIssuedAt = remoteTestResult.result?.sampleDate!!,
            jsonData = rawResponse
        )

        // Save entity in database
        holderDatabase.eventGroupDao().insertAll(listOf(entity))
    }

    override suspend fun saveRemoteEventsNegativeTests(remoteEventsNegativeTests: Map<RemoteEventsNegativeTests, ByteArray>) {
        // Map remote events to EventGroupEntity to save in the database
        val entities = remoteEventsNegativeTests.map {
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = it.key.providerIdentifier ?: error("providerIdentifier is required"),
                type = EventType.Vaccination,
                maxIssuedAt = it.key.events?.map { event -> event.getDate() }
                    ?.maxByOrNull { date -> date.toEpochDay() }
                    ?.atStartOfDay()?.atOffset(
                        ZoneOffset.UTC
                    ) ?: error("At least one event must be present with a date"),
                jsonData = it.value
            )
        }

        // Save entities in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }
}
