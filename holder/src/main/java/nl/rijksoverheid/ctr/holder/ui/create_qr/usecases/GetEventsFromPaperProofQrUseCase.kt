package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONException

interface GetEventsFromPaperProofQrUseCase {
    fun get(qrCode: String): RemoteProtocol3
}

class GetEventsFromPaperProofQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val remoteEventUtil: RemoteEventUtil
) : GetEventsFromPaperProofQrUseCase {

    @Throws(NullPointerException::class, JSONException::class)
    override fun get(qrCode: String): RemoteProtocol3 {
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
                RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC_WITH_UNIQUE.replace("[hkvi]", event.unique ?: "")
            }
            else -> {
                RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC
            }
        }

        return RemoteProtocol3(
            providerIdentifier = providerIdentifier,
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder,
            events = listOf(event)
        )
    }
}