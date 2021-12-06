package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.fakeAndroidUtil
import nl.rijksoverheid.ctr.verifier.fakeScanLogRepository
import nl.rijksoverheid.ctr.verifier.fakeVerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLog
import nl.rijksoverheid.ctr.verifier.ui.scanlog.repositories.ScanLogRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class GetScanLogItemsUseCaseImplTest {

    @Test
    fun `getItems returns correct items if no entities in database`() = runBlocking {
        val firstInstallTime = OffsetDateTime.ofInstant(
            Instant.parse("2021-01-01T00:00:00.00Z"),
            ZoneId.of("UTC")
        )
        val scanLogs = listOf<ScanLog>()
        val scanLogStorageSeconds = 120

        val androidUtil = mockk<AndroidUtil>()
        coEvery { androidUtil.getFirstInstallTime() } answers { firstInstallTime }

        val scanLogRepository = mockk<ScanLogRepository>()
        coEvery { scanLogRepository.getAll() } answers { scanLogs }

        val verifierCachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        coEvery { verifierCachedAppConfigUseCase.getCachedAppConfig() } answers { VerifierConfig.default(
            scanLogStorageSeconds = scanLogStorageSeconds
        ) }

        val usecase = GetScanLogItemsUseCaseImpl(androidUtil, scanLogRepository, verifierCachedAppConfigUseCase)

        val items = usecase.getItems()
        val expectedItems = listOf(
            ScanLogItem.HeaderItem(
                scanLogStorageMinutes = 2
            ),
            ScanLogItem.ListHeaderItem(
                scanLogStorageMinutes = 2
            ),
            ScanLogItem.ListEmptyItem,
            ScanLogItem.FirstInstallTimeItem(
                firstInstallTime = firstInstallTime
            )
        )

        assertEquals(expectedItems, items)
    }

    @Test
    fun `getItems returns correct items if entities in database`() = runBlocking {
        val firstInstallTime = OffsetDateTime.ofInstant(
            Instant.parse("2021-01-01T00:00:00.00Z"),
            ZoneId.of("UTC")
        )
        val scanLogs = listOf(
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy2G,
                count = 2,
                skew = false,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:00:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:10:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy3G,
                count = 2,
                skew = false,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:12:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:15:00.00Z"),
                    ZoneId.of("UTC")
                )
            )
        )
        val scanLogStorageSeconds = 120

        val androidUtil = mockk<AndroidUtil>()
        coEvery { androidUtil.getFirstInstallTime() } answers { firstInstallTime }

        val scanLogRepository = mockk<ScanLogRepository>()
        coEvery { scanLogRepository.getAll() } answers { scanLogs }

        val verifierCachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>()
        coEvery { verifierCachedAppConfigUseCase.getCachedAppConfig() } answers { VerifierConfig.default(
            scanLogStorageSeconds = scanLogStorageSeconds
        ) }

        val usecase = GetScanLogItemsUseCaseImpl(androidUtil, scanLogRepository, verifierCachedAppConfigUseCase)

        val items = usecase.getItems()
        val expectedItems = listOf(
            ScanLogItem.HeaderItem(
                scanLogStorageMinutes = 2
            ),
            ScanLogItem.ListHeaderItem(
                scanLogStorageMinutes = 2
            ),
            ScanLogItem.ListScanLogItem(
                scanLog = scanLogs[0],
                index = 0
            ),
            ScanLogItem.ListScanLogItem(
                scanLog = scanLogs[1],
                index = 1
            ),
            ScanLogItem.FirstInstallTimeItem(
                firstInstallTime = firstInstallTime
            )
        )

        assertEquals(expectedItems, items)
    }
}