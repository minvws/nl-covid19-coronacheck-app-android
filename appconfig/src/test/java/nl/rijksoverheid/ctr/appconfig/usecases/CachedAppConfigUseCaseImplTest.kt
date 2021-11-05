package nl.rijksoverheid.ctr.appconfig.usecases

import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigStorageManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import org.junit.Assert.*
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

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertTrue(cachedAppConfigUseCase.isCachedAppConfigValid())
    }

    @Test
    fun `different parseable file type returns false`() {
        mockWithFileContents("{\"bar\":\"foo\"}")

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertFalse(cachedAppConfigUseCase.isCachedAppConfigValid())
    }

    @Test
    fun `invalid file returns false`() {
        mockWithFileContents("{\"androidMinimumVersion\":1025,\"appDeactivated\":false,\"informationURL\":\"https://coronacheck.nl\",\"requireUpdateBefore\":1620781181,\"temporarilyDisabled\":false,\"recoveryEventValidity\":7300,\"testEventValidity\":40,\"domesticCredentialValidity\":24,\"credentialRenewalDays\":5,\"configTTL\":259200,\"maxValidityHours\":40,\"vaccinationEventValidity\":14600,\"euLaunchDate\":\"2021-07-01T00:00:00Z\",\"hpkC")

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertFalse(cachedAppConfigUseCase.isCachedAppConfigValid())
    }

    @Test
    fun `when it's the verifier app, app config should be verifier config`() {
        mockWithFileContents(VerifierConfig.default(verifierInformationURL = "test").toJson(moshi))

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, true)

        cachedAppConfigUseCase.getCachedAppConfig().run {
            assertTrue(this is VerifierConfig)
            assertEquals(informationURL, "test")
        }

    }

    @Test
    fun `when it's the holder app, app config should be holder config`() {
        mockWithFileContents(HolderConfig.default(holderInformationURL = "test").toJson(moshi))

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        cachedAppConfigUseCase.getCachedAppConfig().run {
            assertTrue(this is HolderConfig)
            assertEquals(informationURL, "test")
        }
    }

    @Test
    fun `get default app config when file can't be parsed`() {
        every { appConfigStorageManager.getFileAsBufferedSource(any()) } returns null

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertEquals(cachedAppConfigUseCase.getCachedAppConfig(), HolderConfig.default())
    }

    @Test
    fun `given a config payload, check if config hash is correct`() {
        every { appConfigStorageManager.getFileAsBufferedSource(any()) } returns "{\"androidMinimumVersion\":1,\"androidRecommendedVersion\":2046,\"androidMinimumVersionMessage\":\"Om de app te gebruiken heb je de laatste versie uit de store nodig.\",\"playStoreURL\":\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=nl.rijksoverheid.ctr.verifier\",\"iosMinimumVersion\":\"1.0.0\",\"iosRecommendedVersion\":\"2.2.0\",\"iosMinimumVersionMessage\":\"Om de app te gebruiken heb je de laatste versie uit de store nodig.\",\"iosAppStoreURL\":\"https:\\/\\/apps.apple.com\\/nl\\/app\\/scanner-voor-coronacheck\\/id1549842661\",\"appDeactivated\":false,\"configTTL\":300,\"configMinimumIntervalSeconds\":60,\"upgradeRecommendationInterval\":24,\"maxValidityHours\":40,\"clockDeviationThresholdSeconds\":30,\"informationURL\":\"https:\\/\\/coronacheck.nl\",\"defaultEvent\":\"cce4158f-582f-49c0-9d4d-611ce3866999\",\"universalLinkDomains\":[{\"url\":\"web.acc.coronacheck.nl\",\"name\":\"CoronaCheck app\"}],\"domesticVerificationRules\":{\"qrValidForSeconds\":60,\"proofIdentifierDenylist\":{\"STFNx7A24ZI1u5WDX8X9BA==\":true}},\"europeanVerificationRules\":{\"testAllowedTypes\":[\"LP217198-3\",\"LP6464-4\"],\"testValidityHours\":25,\"vaccinationValidityDelayBasedOnVaccinationDate\":true,\"vaccinationValidityDelayIntoForceDate\":\"2021-07-06\",\"vaccinationValidityDelayDays\":14,\"vaccinationJanssenValidityDelayDays\":28,\"vaccinationJanssenValidityDelayIntoForceDate\":\"2021-07-24\",\"vaccineAllowedProducts\":[\"EU\\/1\\/20\\/1528\",\"EU\\/1\\/20\\/1507\",\"EU\\/1\\/21\\/1529\",\"EU\\/1\\/20\\/1525\",\"Covishield\",\"BBIBP-CorV\",\"CoronaVac\"],\"recoveryValidFromDays\":11,\"recoveryValidUntilDays\":180,\"proofIdentifierDenylist\":{\"7EXmXBhfyBZJgt1dki0cfQ==\":true}}}".toResponseBody("application/json".toMediaType()).source()

        val cachedAppConfigUseCase =
            CachedAppConfigUseCaseImpl(appConfigStorageManager, "", moshi, false)

        assertEquals("5f253c8", cachedAppConfigUseCase.getCachedAppConfigHash())
    }
}