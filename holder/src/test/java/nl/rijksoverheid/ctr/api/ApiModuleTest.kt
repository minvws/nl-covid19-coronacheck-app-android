package nl.rijksoverheid.ctr.api

import nl.rijksoverheid.ctr.api.signing.certificates.EV_ROOT_CA
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.tls.decodeCertificatePem
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ApiModuleTest : AutoCloseKoinTest() {

    @Test
    fun `given our http client, when doing a network request, then ssl is using only our certificate`() {
        loadKoinModules(apiModule(
            baseUrl = "https://holder-api.acc.coronacheck.nl/v4/".toHttpUrl(),
            signatureCertificateCnMatch = "",
            coronaCheckApiChecks = true,
            testProviderApiChecks = true
        ))
        val okHttpClient: OkHttpClient by inject()
        val acceptedIssuers = okHttpClient.x509TrustManager!!.acceptedIssuers
        assertEquals(1, acceptedIssuers.size)
        assertEquals(acceptedIssuers.first(), EV_ROOT_CA.decodeCertificatePem())
    }
}
