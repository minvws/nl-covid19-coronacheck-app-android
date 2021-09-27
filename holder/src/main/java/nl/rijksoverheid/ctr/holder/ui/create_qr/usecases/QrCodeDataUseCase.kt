package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface QrCodeDataUseCase {
    suspend fun getQrCodeData(greenCardType: GreenCardType,
                              originType: OriginType,
                              credential: ByteArray,
                              shouldDisclose: Boolean,
                              qrCodeWidth: Int,
                              qrCodeHeight: Int): QrCodeData
}

class QrCodeDataUseCaseImpl(private val qrCodeUseCase: QrCodeUseCase,
                            private val greenCardUtil: GreenCardUtil,
                            private val mobileCoreWrapper: MobileCoreWrapper,
                            private val readEuropeanCredentialUtil: ReadEuropeanCredentialUtil): QrCodeDataUseCase {

    override suspend fun getQrCodeData(
                      greenCardType: GreenCardType,
                      originType: OriginType,
                      credential: ByteArray,
                      shouldDisclose: Boolean,
                      qrCodeWidth: Int,
                      qrCodeHeight: Int): QrCodeData {

        val qrCodeBitmap = qrCodeUseCase.qrCode(
            credential = credential,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight,
            shouldDisclose = shouldDisclose,
            errorCorrectionLevel = greenCardUtil.getErrorCorrectionLevel(greenCardType)
        )

        return when (greenCardType) {
            is GreenCardType.Domestic -> {
                QrCodeData.Domestic(
                    bitmap = qrCodeBitmap,
                    readDomesticCredential = mobileCoreWrapper.readDomesticCredential(credential)
                )
            }

            is GreenCardType.Eu -> {
                val readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential)
                if (originType is OriginType.Vaccination) {
                    QrCodeData.European.Vaccination(
                        bitmap = qrCodeBitmap,
                        readEuropeanCredential = readEuropeanCredential,
                        dosis = readEuropeanCredentialUtil.getDosisForVaccination(readEuropeanCredential)
                    )
                } else {
                    QrCodeData.European.NonVaccination(
                        bitmap = qrCodeBitmap,
                        readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential)
                    )
                }
            }
        }
    }
}