package nl.rijksoverheid.ctr.qrcodes.usecases

import io.mockk.every
import io.mockk.mockk
import java.time.OffsetDateTime
import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.fakeGreenCardUtil
import nl.rijksoverheid.ctr.fakeMobileCoreWrapper
import nl.rijksoverheid.ctr.fakeQrCodeUsecase
import nl.rijksoverheid.ctr.fakeReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodesResult
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class QrCodesResultUseCaseImplTest : AutoCloseKoinTest() {

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
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic recovery QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic test QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for european recovery QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns SingleQrCode for european test QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }

    @Test
    fun `getQrCodesResult returns MultipleQrCodes for european vaccination QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            qrCodeFragmentData = QrCodeFragmentData(
                type = GreenCardType.Eu,
                originType = OriginType.Vaccination,
                credentialsWithExpirationTime = listOf(Pair("".toByteArray(), OffsetDateTime.now())),
                shouldDisclose = QrCodeFragmentData.ShouldDisclose.Disclose(1, GreenCardDisclosurePolicy.ThreeG)
            ),
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.MultipleQrCodes)
    }
}
