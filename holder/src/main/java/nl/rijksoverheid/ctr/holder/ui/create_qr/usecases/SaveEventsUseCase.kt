package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {
    suspend fun save(remoteTestResult: List<SignedResponseWithModel<RemoteEvents>>)
}

class SaveEventsUseCaseImpl(private val holderDatabase: HolderDatabase) : SaveEventsUseCase {

    override suspend fun save(remoteTestResult: List<SignedResponseWithModel<RemoteEvents>>) {
        // Make remote test results to event group entities to save in the database
        val entities = remoteTestResult.map { signedResponseWithModel ->
            val model = signedResponseWithModel.model
            EventGroupEntity(
                walletId = 1,
                providerIdentifier = signedResponseWithModel.model.providerIdentifier,
                type = EventType.Vaccination,
                maxIssuedAt = model.events.map { it.getDate() }.maxByOrNull { it.toEpochDay() }!!,
                jsonData = signedResponseWithModel.rawResponse
            )
        }

        // Save entities in database
        holderDatabase.eventGroupDao().insertAll(entities)
    }
}
