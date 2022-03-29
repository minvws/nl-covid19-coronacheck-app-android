package nl.rijksoverheid.ctr.holder.api

import nl.rijksoverheid.ctr.api.signing.http.SignedRequest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import retrofit2.http.GET

interface RemoteConfigApiClient {
    @GET("holder/config_providers")
    @SignedRequest
    suspend fun getConfigCtp(): RemoteConfigProviders
}