package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodesResult
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class QrCodesResultUseCaseImplTest {

    private val usecase = QrCodesResultUseCaseImpl(
        qrCodeUseCase = fakeQrCodeUsecase(),
        greenCardUtil = fakeGreenCardUtil(),
        mobileCoreWrapper = fakeMobileCoreWrapper(),
        readEuropeanCredentialUtil = fakeReadEuropeanCredentialUtil()
    )

    @Test
    fun `getQrCodesResult returns SingleQrCode for domestic QR`() = runBlocking {
        val result = usecase.getQrCodesResult(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination,
            credentials = listOf("".toByteArray()),
            shouldDisclose = true,
            qrCodeHeight = 0,
            qrCodeWidth = 0
        )
        assertTrue(result is QrCodesResult.SingleQrCode)
    }
}