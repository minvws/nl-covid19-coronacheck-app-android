package nl.rijksoverheid.ctr.holder.pdf

import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.junit.Test

class PrintExportDccUseCaseImplTest {
    private val holderDatabase = mockk<HolderDatabase>()
    private val mobileCoreWrapper = mockk<MobileCoreWrapper>()

    @Test
    fun `green cards export return json string`() = runTest {
        val credential = mockk<CredentialEntity> {
            coEvery { data } returns "".toByteArray()
            coEvery { expirationTime } returns OffsetDateTime.parse("2023-07-21T10:15:30+01:00")
        }
        val greenCard = mockk<GreenCard>(relaxed = true) {
            coEvery { greenCardEntity.type } returns GreenCardType.Eu
            coEvery { credentialEntities } returns listOf(credential)
        }
        coEvery { holderDatabase.greenCardDao().getAll() } returns listOf(greenCard)
        coEvery { mobileCoreWrapper.readEuropeanCredential(any()) } returns mockk(relaxed = true)
        val printExportDccUseCase = PrintExportDccUseCaseImpl(
            holderDatabase,
            mobileCoreWrapper,
            Moshi.Builder().add(OffsetDateTimeJsonAdapter()).build()
        )

        val jsonString = printExportDccUseCase.export()

        assertEquals("{\"european\":[{\"dcc\":{},\"expirationTime\":\"2023-07-21T10:15:30+01:00\",\"qr\":\"\"}]}", jsonString)
    }
}
