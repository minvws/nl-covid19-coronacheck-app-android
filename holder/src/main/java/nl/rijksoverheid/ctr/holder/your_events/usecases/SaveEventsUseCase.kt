/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtil
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventHolderUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3Util
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SaveEventsUseCase {

    suspend fun saveRemoteProtocols3(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        removePreviousEvents: Boolean,
        flow: Flow
    ): SaveEventsUseCaseImpl.SaveEventResult

    suspend fun remoteProtocols3AreConflicting(remoteProtocols: Map<RemoteProtocol, ByteArray>): ConflictingEventResult
}

class SaveEventsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val remoteEventHolderUtil: RemoteEventHolderUtil,
    private val scopeUtil: ScopeUtil,
    private val remoteEventUtil: RemoteEventUtil,
    private val remoteProtocol3Util: RemoteProtocol3Util
) : SaveEventsUseCase {

    private suspend fun remoteEventExistsAlready(remoteEvents: List<RemoteEvent>): Boolean {
        val remoteEventUniques = remoteEvents.mapNotNull { it.unique }
        val storedEventIdentifiers = holderDatabase.eventGroupDao().getAll().map { it.providerIdentifier }
        return storedEventIdentifiers.any { identifier ->
            remoteEventUniques.any { remoteEventUtil.isDccEvent(identifier) && identifier.contains(it) }
        }
    }

    override suspend fun remoteProtocols3AreConflicting(remoteProtocols: Map<RemoteProtocol, ByteArray>): ConflictingEventResult {
        if (remoteEventExistsAlready(remoteProtocols.map { it.key }.flatMap { it.events ?: emptyList() })) {
            return ConflictingEventResult.Existing
        }

        val storedEventHolders = holderDatabase.eventGroupDao().getAll()
            .mapNotNull { remoteEventHolderUtil.holder(it.jsonData, it.providerIdentifier) }
            .distinct()
        val incomingEventHolders = remoteProtocols.mapNotNull { it.key.holder }.distinct()

        return if (remoteEventHolderUtil.conflicting(storedEventHolders, incomingEventHolders)) {
            ConflictingEventResult.Holder
        } else {
            ConflictingEventResult.None
        }
    }

    override suspend fun saveRemoteProtocols3(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        removePreviousEvents: Boolean,
        flow: Flow
    ): SaveEventResult {
        try {
            if (removePreviousEvents) {
                holderDatabase.eventGroupDao().deleteAll()
            }

            val entities = remoteProtocols.map {
                val remoteProtocol = it.key
                val remoteEvents = remoteProtocol.events ?: listOf()
                val originType = remoteEventUtil.getOriginType(remoteEvents.first())
                EventGroupEntity(
                    walletId = 1,
                    providerIdentifier = remoteProtocol3Util.getProviderIdentifier(remoteProtocol),
                    type = originType,
                    jsonData = it.value,
                    scope = scopeUtil.getScopeForOriginType(
                        originType = originType,
                        getPositiveTestWithVaccination = flow == HolderFlow.VaccinationAndPositiveTest
                    ),
                    expiryDate = null,
                    draft = true
                )
            }

            // Save entity in database
            holderDatabase.eventGroupDao().insertAll(entities)
        } catch (e: Exception) {
            return SaveEventResult.Failed(
                errorResult = AppErrorResult(
                    step = HolderStep.StoringEvents,
                    e = e
                )
            )
        }
        return SaveEventResult.Success
    }

    sealed class SaveEventResult {
        object Success : SaveEventResult()
        data class Failed(val errorResult: ErrorResult) : SaveEventResult()
    }
}
