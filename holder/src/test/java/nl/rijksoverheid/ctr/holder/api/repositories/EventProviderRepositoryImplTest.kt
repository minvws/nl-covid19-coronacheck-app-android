package nl.rijksoverheid.ctr.holder.api.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.api.signing.certificates.DIGICERT_BTC_ROOT_CA
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import nl.rijksoverheid.ctr.holder.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.api.TestProviderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteUnomi
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

class EventProviderRepositoryImplTest {
    private val testProviderApiClientUtil: TestProviderApiClientUtil = mockk()
    private val networkRequestResultFactory: NetworkRequestResultFactory = mockk(relaxed = true)
    private val eventProviderRepository: EventProviderRepository =
        EventProviderRepositoryImpl(testProviderApiClientUtil, networkRequestResultFactory)
    private val testProviderApiClient: TestProviderApiClient = mockk(relaxed = true)
    private val cmsCertificatesBytes = listOf(PRIVATE_ROOT_CA.toByteArray())
    private val tlsCertificateBytes = listOf(DIGICERT_BTC_ROOT_CA.toByteArray())
    private val tlsCertificateSlot = slot<List<ByteArray>>()
    private val cmsCertificateSlot = slot<List<ByteArray>>()

    @Before
    fun setup() {
        coEvery { testProviderApiClientUtil.client(capture(tlsCertificateSlot), capture(cmsCertificateSlot)) } returns testProviderApiClient
    }

    @Suppress("UNCHECKED_CAST")
    private fun <R : Any> mockRequestResult() {
        coEvery {
            networkRequestResultFactory.createResult(
                step = any(),
                provider = any(),
                interceptHttpError = any<suspend (HttpException) -> R>(),
                networkCall = any<suspend () -> NetworkRequestResult<R>>()
            )
        } coAnswers {
            val fourthArg = args[3] as suspend () -> NetworkRequestResult<R>
            fourthArg.invoke()
        }
    }

    @Test
    fun `pin with the passed certificates the umomi request`() = runTest {
        mockRequestResult<SignedResponseWithModel<RemoteUnomi>>()
        val signingCertificateSlot = slot<SigningCertificate>()

        eventProviderRepository.getUnomi(
            "url",
            "token",
            "filter",
            "scope",
            cmsCertificatesBytes,
            "provider",
            tlsCertificateBytes
        )

        assertEquals(tlsCertificateBytes.first(), tlsCertificateSlot.captured.first())
        assertEquals(cmsCertificatesBytes.first(), cmsCertificateSlot.captured.first())
        coVerify(exactly = 1) {
            testProviderApiClient.getUnomi(
                "url",
                "Bearer token",
                "3.0",
                any(),
                capture(signingCertificateSlot)
            )
        }
        assertEquals(
            cmsCertificatesBytes.first(),
            signingCertificateSlot.captured.certificateBytes.first()
        )
    }

    @Test
    fun `pin with the passed certificates the getEvents request`() = runTest {
        mockRequestResult<SignedResponseWithModel<RemoteProtocol>>()
        val signingCertificateSlot = slot<SigningCertificate>()

        eventProviderRepository.getEvents(
            "url",
            "token",
            cmsCertificatesBytes,
            "filter",
            "scope",
            "provider",
            tlsCertificateBytes
        )

        assertEquals(tlsCertificateBytes.first(), tlsCertificateSlot.captured.first())
        assertEquals(cmsCertificatesBytes.first(), cmsCertificateSlot.captured.first())
        coVerify(exactly = 1) {
            testProviderApiClient.getEvents(
                "url",
                "Bearer token",
                "3.0",
                any(),
                capture(signingCertificateSlot)
            )
        }
        assertEquals(
            cmsCertificatesBytes.first(),
            signingCertificateSlot.captured.certificateBytes.first()
        )
    }
}
