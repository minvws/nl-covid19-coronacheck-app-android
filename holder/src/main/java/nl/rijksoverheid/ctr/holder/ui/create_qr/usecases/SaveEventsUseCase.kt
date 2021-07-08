package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import androidx.room.withTransaction
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {
    suspend fun saveNegativeTest2(negativeTest2: RemoteTestResult2, rawResponse: ByteArray)
    suspend fun saveRemoteProtocols3(
        remoteProtocols3: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType
    )
}

class SaveEventsUseCaseImpl(private val holderDatabase: HolderDatabase) : SaveEventsUseCase {

    override suspend fun saveNegativeTest2(
        negativeTest2: RemoteTestResult2,
        rawResponse: ByteArray
    ) {
        // Make remote test results to event group entities to save in the database
        val entity = EventGroupEntity(
            walletId = 1,
            providerIdentifier = negativeTest2.providerIdentifier,
            type = OriginType.Test,
            maxIssuedAt = negativeTest2.result?.sampleDate!!,
            jsonData = rawResponse
        )

        // Save entity in database
        holderDatabase.eventGroupDao().insertAll(listOf(entity))
    }


    override suspend fun saveRemoteProtocols3(
        remoteProtocols3: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType
    ) {
        val entities = remoteProtocols3.map { remoteProtocol3 ->
            val remoteEvents = remoteProtocol3.key.events ?: listOf()
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = remoteProtocol3.key.providerIdentifier,
                type = originType,
                maxIssuedAt = getMaxIssuedAt(remoteEvents),
                jsonData = remoteProtocol3.value
            )
        }

        // Save entity in database
        holderDatabase.run {
            withTransaction {
                // Delete all previous events of type Vaccination or Recovery so that if
                // for example person a has vaccination events saved, person b vaccination
                // gets overwritten. This is just a temporary quick fix until we support
                // multiple wallets in the app
                if (originType == OriginType.Vaccination || originType == OriginType.Recovery) {
                    eventGroupDao().deleteAllOfType(originType)
                }
                eventGroupDao().insertAll(entities)
            }
        }
    }

    private fun getMaxIssuedAt(remoteEvents: List<RemoteEvent>): OffsetDateTime {
        return remoteEvents.map { event -> event.getDate() }
            .maxByOrNull { date -> date?.toEpochSecond() ?: error("Date should not be null") }
            ?: error("At least one event must be present with a date")
    }
}
