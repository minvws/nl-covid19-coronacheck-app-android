package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface QrCodeDataUseCase {
    suspend fun getQrCodeData(greenCardType: GreenCardType,
                              credential: ByteArray,
                              shouldDisclose: Boolean,
                              qrCodeWidth: Int,
                              qrCodeHeight: Int): QrCodeData
}

class QrCodeDataUseCaseImpl(private val qrCodeUseCase: QrCodeUseCase,
                        private val mobileCoreWrapper: MobileCoreWrapper): QrCodeDataUseCase {

    override suspend fun getQrCodeData(greenCardType: GreenCardType,
                      credential: ByteArray,
                      shouldDisclose: Boolean,
                      qrCodeWidth: Int,
                      qrCodeHeight: Int): QrCodeData {

        val qrCodeBitmap = qrCodeUseCase.qrCode(
            credential = credential,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight,
            shouldDisclose = shouldDisclose
        )

        return when (greenCardType) {
            is GreenCardType.Domestic -> {
                QrCodeData.Domestic(
                    bitmap = qrCodeBitmap,
                    readDomesticCredential = mobileCoreWrapper.readDomesticCredential(credential)
                )
            }

            is GreenCardType.Eu -> {
                QrCodeData.European(
                    bitmap = qrCodeBitmap,
                    readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential)
                )
            }
        }
    }
}