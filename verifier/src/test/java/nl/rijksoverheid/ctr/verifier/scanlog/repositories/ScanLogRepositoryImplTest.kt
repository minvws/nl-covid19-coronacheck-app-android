package nl.rijksoverheid.ctr.verifier.scanlog.repositories

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.scanlog.datamapper.ScanLogDataMapper
import org.junit.Test

class ScanLogRepositoryImplTest {

    @Test
    fun `insert calls correct method to insert entity into database`() = runBlocking {
        val verifierDatabase = mockk<VerifierDatabase>(relaxed = true)
        val scanLogDataMapper = mockk<ScanLogDataMapper>(relaxed = true)

        val repository = ScanLogRepositoryImpl(
            verifierDatabase,
            scanLogDataMapper
        )

        val entity = ScanLogEntity(
            id = 0,
            policy = VerificationPolicy.VerificationPolicy1G,
            date = Instant.parse("2021-01-01T00:00:00.00Z")
        )

        repository.insert(
            entity
        )

        coVerify { verifierDatabase.scanLogDao().insert(entity) }
    }

    @Test
    fun `getAll calls correct methods to get transform entities from database`() = runBlocking {
        val verifierDatabase = mockk<VerifierDatabase>(relaxed = true)
        val scanLogDataMapper = mockk<ScanLogDataMapper>(relaxed = true)

        val repository = ScanLogRepositoryImpl(
            verifierDatabase,
            scanLogDataMapper
        )

        val entity = ScanLogEntity(
            id = 0,
            policy = VerificationPolicy.VerificationPolicy1G,
            date = Instant.parse("2021-01-01T00:00:00.00Z")
        )

        coEvery { verifierDatabase.scanLogDao().getAll() } answers { listOf(entity) }

        repository.getAll()

        coVerify { verifierDatabase.scanLogDao().getAll() }
        coVerify { scanLogDataMapper.transform(listOf(entity)) }
    }
}
