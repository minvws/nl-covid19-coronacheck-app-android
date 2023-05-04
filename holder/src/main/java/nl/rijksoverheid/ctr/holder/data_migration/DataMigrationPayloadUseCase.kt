package nl.rijksoverheid.ctr.holder.data_migration

import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.SignedResponse
import org.json.JSONObject

interface DataMigrationPayloadUseCase {
    fun parsePayload(jsonData: ByteArray): RemoteProtocol?
}

class DataMigrationPayloadUseCaseImpl(
    private val moshi: Moshi,
    private val getEventsFromPaperProofQrUseCase: GetEventsFromPaperProofQrUseCase
) : DataMigrationPayloadUseCase {
    override fun parsePayload(jsonData: ByteArray): RemoteProtocol? {
        val json = JSONObject(jsonData.decodeToString())

        return if (json.has("credential")) {
            // dcc
            getEventsFromPaperProofQrUseCase.get(json.getString("credential"))
        } else {
            // non dcc
            val payload = moshi.adapter(SignedResponse::class.java)
                .fromJson(String(jsonData))?.payload
            val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
            return moshi.adapter(RemoteProtocol::class.java).fromJson(decodedPayload)
        }
    }
}
