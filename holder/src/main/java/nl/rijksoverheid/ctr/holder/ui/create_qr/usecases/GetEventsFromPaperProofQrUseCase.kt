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

        return RemoteProtocol3(
            providerIdentifier = RemoteConfigProviders.EventProvider.PROVIDER_IDENTIFIER_DCC,
            protocolVersion = "3.0",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder,
            events = listOf(event)
        )
    }
}