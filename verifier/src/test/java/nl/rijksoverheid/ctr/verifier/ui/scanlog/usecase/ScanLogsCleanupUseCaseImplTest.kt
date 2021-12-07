package nl.rijksoverheid.ctr.verifier.ui.scanlog.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.persistance.usecase.VerifierCachedAppConfigUseCase
import org.junit.Test
import java.time.*

class ScanLogsCleanupUseCaseImplTest {

    @Test
    fun `cleanup removes all entities from database that needs to be removed`() = runBlocking {
        val entities = listOf(
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy2G,
                date = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:00:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy2G,
                date = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:10:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy2G,
                date = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:20:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy2G,
                date = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:30:00.00Z"),
                    ZoneId.of("UTC")
                )
            )
        )

        val entitiesToRemove = listOf(
            entities[0],
            entities[1]
        )

        val verifierDatabase = mockk<VerifierDatabase>(relaxed = true)
        coEvery { verifierDatabase.scanLogDao().getAll() } answers { entities }

        val verifierCachedAppConfigUseCase = mockk<VerifierCachedAppConfigUseCase>(relaxed = true)
        coEvery { verifierCachedAppConfigUseCase.getCachedAppConfig().scanLogStorageSeconds } answers { 599 }

        val usecase = ScanLogsCleanupUseCaseImpl(
            clock = Clock.fixed(Instant.parse("2021-01-01T00:20:00.00Z"), ZoneId.of("UTC")),
            verifierDatabase = verifierDatabase,
            verifierCachedAppConfigUseCase = verifierCachedAppConfigUseCase
        )

        usecase.cleanup()

        coVerify { verifierDatabase.scanLogDao().delete(entitiesToRemove) }
    }
}