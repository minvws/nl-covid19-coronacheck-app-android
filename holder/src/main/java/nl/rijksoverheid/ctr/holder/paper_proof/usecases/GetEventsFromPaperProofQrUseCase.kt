/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONException

interface GetEventsFromPaperProofQrUseCase {
    fun get(qrCode: String): RemoteProtocol
}

class GetEventsFromPaperProofQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val remoteEventUtil: RemoteEventUtil
) : GetEventsFromPaperProofQrUseCase {

    @Throws(NullPointerException::class, JSONException::class)
    override fun get(qrCode: String): RemoteProtocol {
        val credential = qrCode.toByteArray()
        val credentials = mobileCoreWrapper.readEuropeanCredential(credential)
        val dcc = credentials.optJSONObject("dcc")
        val holder = remoteEventUtil.getHolderFromDcc(dcc!!)
        val event = remoteEventUtil.getRemoteEventFromDcc(dcc)

        val providerIdentifier = when (event) {
            is RemoteEventVaccination -> {
                // For hkvi vaccination events we want to be able to save multiple events (for example you get 2 papers, one with your first vaccination and another with your second)
                // The database prevents us from doing so because it has uniques on both providerIdentifier and type
                // For hkvi vaccinations we add the unique to the provider identifier so it gets saved as well
                RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC_SUFFIX.replace("[unique]", event.unique ?: "")
            }
            else -> {
                RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC
            }
        }

        return RemoteProtocol(
            providerIdentifier = providerIdentifier,
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder,
            events = listOf(event)
        )
    }
}