package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.dao.OriginDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.OffsetDateTime
import kotlin.test.assertFalse

class HasOriginUseCaseImplTest {

    private val database: HolderDatabase = mockk()
    private val originDao: OriginDao = mockk()
    private val usecase: HasOriginUseCaseImpl = HasOriginUseCaseImpl(
        database = database
    )

    @Before
    fun setup() {
        every { database.originDao() } answers { originDao }
    }


    @Test
    fun `hasOrigin returns true if origin exists`() = runBlocking {
        val originEntity = OriginEntity(
            id = 0,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now(),
            validFrom = OffsetDateTime.now()
        )

        coEvery { originDao.getAll() } answers { listOf(originEntity) }
        assertTrue(usecase.hasOrigin(OriginType.Test))
    }

    @Test
    fun `hasOrigin returns false if origin does not exists`() = runBlocking {
        val originEntity = OriginEntity(
            id = 0,
            greenCardId = 1,
            type = OriginType.Test,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now(),
            validFrom = OffsetDateTime.now()
        )

        coEvery { originDao.getAll() } answers { listOf(originEntity) }
        assertFalse(usecase.hasOrigin(OriginType.Vaccination))
    }

}