package nl.rijksoverheid.ctr.holder.data_migration

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataMigrationUseCaseImplTest {

    private val holderDatabase = mockk<HolderDatabase>()
    private val dataMigrationUseCase = DataMigrationUseCaseImpl(holderDatabase)

    @Test
    fun `canTransferData returns false for draft only events`() = runTest {
        val events = listOf(
            mockk<EventGroupEntity>().apply {
                coEvery { draft } returns true
            }
        )
        coEvery { holderDatabase.eventGroupDao().getAll() } returns events

        assertFalse(dataMigrationUseCase.canTransferData())
    }

    @Test
    fun `canTransferData returns true if non draft events exist`() = runTest {
        val events = listOf(
            mockk<EventGroupEntity>().apply {
                coEvery { draft } returns true
            },
            mockk<EventGroupEntity>().apply {
                coEvery { draft } returns false
            }
        )
        coEvery { holderDatabase.eventGroupDao().getAll() } returns events

        assertTrue(dataMigrationUseCase.canTransferData())
    }
}
