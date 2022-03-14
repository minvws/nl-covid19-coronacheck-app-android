package nl.rijksoverheid.ctr.qrcodes.usecases

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodesResult
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCaseImpl
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert.assertTrue
import org.junit.Test

class QrCodesResultUseCaseImplTest {

    private val usecase = QrCodesResultUseCaseImpl(
        qrCodeUseCase = fakeQrCodeUsecase(),
        greenCardUtil = fakeGreenCardUtil(),
        mobileCoreWrapper = fakeMobileCoreWrapper(),
        readEuropeanCredentialUtil = fakeReadEuropeanCredentialUtil(),
        credentialUtil = mockk(relaxed = true),
        multipleQrCodesUtil = mockk {
            every { getMostRelevantQrCodeIndex(any()) } returns 0
        }
    )

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic vaccination QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic recovery QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Recovery,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic test QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0, 
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for european recovery QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for european test QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Test,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns MultipleQrCodes for european vaccination QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination,
            credentials = listOf("".toByteArray()),
            shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(GreenCardDisclosurePolicy.ThreeG),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.MultipleQrCodes)
    }
}