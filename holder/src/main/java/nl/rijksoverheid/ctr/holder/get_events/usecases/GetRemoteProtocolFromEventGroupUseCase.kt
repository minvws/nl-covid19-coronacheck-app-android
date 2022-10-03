/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import org.json.JSONObject

interface GetRemoteProtocolFromEventGroupUseCase {
    fun get(eventGroup: EventGroupEntity): RemoteProtocol?
}

class GetRemoteProtocolFromEventGroupUseCaseImpl(
    private val remoteEventUtil: RemoteEventUtil,
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase
) : GetRemoteProtocolFromEventGroupUseCase {

    override fun get(eventGroup: EventGroupEntity): RemoteProtocol? {
        val isDccEvent = remoteEventUtil.isDccEvent(
            providerIdentifier = eventGroup.providerIdentifier
        )

        return if (isDccEvent) {
            val credential =
                JSONObject(eventGroup.jsonData.decodeToString()).getString("credential")
            getEventsFromPaperProofQrUseCase.get(credential)
        } else {
            remoteEventUtil.getRemoteProtocol3FromNonDcc(
                eventGroupEntity = eventGroup
            )
        }
    }
}
