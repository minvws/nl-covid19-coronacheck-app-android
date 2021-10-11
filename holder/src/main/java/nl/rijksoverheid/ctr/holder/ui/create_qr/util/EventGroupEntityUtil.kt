package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONObject

interface EventGroupEntityUtil {
    suspend fun allVaccinationEvents(eventGroupEntities: List<EventGroupEntity>): List<RemoteEventVaccination>
}

class EventGroupEntityUtilImpl(
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val remoteEventUtil: RemoteEventUtil
): EventGroupEntityUtil {
    override suspend fun allVaccinationEvents(eventGroupEntities: List<EventGroupEntity>): List<RemoteEventVaccination> {
        val dccRemoteEvents = eventGroupEntities
            .filter { it.providerIdentifier == RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC }
            .map {
                val credential = JSONObject(String(it.jsonData)).optString("credential")
                val readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential.toByteArray())
                val dcc = readEuropeanCredential.optJSONObject("dcc")
                remoteEventUtil.getRemoteEventFromDcc(dcc)
            }
            .filterIsInstance<RemoteEventVaccination>()

        val nonDccRemoteEvents = eventGroupEntities
            .filter { it.providerIdentifier != RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC }
            .map { remoteEventUtil.getRemoteEventsFromNonDcc(it) }
            .flatten()
            .filterIsInstance<RemoteEventVaccination>()

        return nonDccRemoteEvents.plus(dccRemoteEvents)
    }
}