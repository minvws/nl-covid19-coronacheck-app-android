package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import okio.BufferedSource
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachedAppConfigUseCaseImplTest {

    private val moshi = Moshi.Builder().build()
    private val appConfigBufferedSource = mockk<BufferedSource>(relaxed = true)
    private val appConfigStorageManager = mockk<AppConfigStorageManager>(relaxed = true)

    private fun mockWithFileContents(fileContents: String) {
        every { appConfigBufferedSource.readUtf8() } returns fileContents
        every { appConfigStorageManager.getFileAsBufferedSource(any()) } returns appConfigBufferedSource
    }

    @Test
    fun `valid file returns true`() {
        mockWithFileContents(HolderConfig.default().toJson(moshi))

        val cachedAppConfigUseCase = CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertTrue(cachedAppConfigUseCase.isCachedAppConfigValid())
    }

    @Test
    fun `different parseable file type returns false`() {
        mockWithFileContents("{\"bar\":\"foo\"}")

        val cachedAppConfigUseCase = CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertFalse(cachedAppConfigUseCase.isCachedAppConfigValid())
    }

    @Test
    fun `invalid file returns false`() {
        mockWithFileContents("{\"androidMinimumVersion\":1025,\"appDeactivated\":false,\"informationURL\":\"https://coronacheck.nl\",\"requireUpdateBefore\":1620781181,\"temporarilyDisabled\":false,\"recoveryEventValidity\":7300,\"testEventValidity\":40,\"domesticCredentialValidity\":24,\"credentialRenewalDays\":5,\"configTTL\":259200,\"maxValidityHours\":40,\"vaccinationEventValidity\":14600,\"euLaunchDate\":\"2021-07-01T00:00:00Z\",\"hpkC")

        val cachedAppConfigUseCase = CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertFalse(cachedAppConfigUseCase.isCachedAppConfigValid())
    }
}