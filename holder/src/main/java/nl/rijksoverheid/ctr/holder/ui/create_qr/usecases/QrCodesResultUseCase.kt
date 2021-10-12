package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.MultipleQrCodesUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodesResult
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/**
 * Get all data needed to display QR codes based on data is send from the dashboard
 */
interface QrCodesResultUseCase {
    suspend fun getQrCodesResult(
        greenCardType: GreenCardType,
        originType: OriginType,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult
}

class QrCodesResultUseCaseImpl(
    private val qrCodeUseCase: QrCodeUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val readEuropeanCredentialUtil: ReadEuropeanCredentialUtil,
    private val credentialUtil: CredentialUtil,
    private val multipleQrCodesUtil: MultipleQrCodesUtil
) : QrCodesResultUseCase {

    override suspend fun getQrCodesResult(
        greenCardType: GreenCardType,
        originType: OriginType,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult {

        return when (greenCardType) {
            is GreenCardType.Domestic -> {
                getQrCodesResultForDomestic(
                    greenCardType = greenCardType,
                    credentials = credentials,
                    shouldDisclose = shouldDisclose,
                    qrCodeWidth = qrCodeWidth,
                    qrCodeHeight = qrCodeHeight
                )
            }

            is GreenCardType.Eu -> {
                if (originType is OriginType.Vaccination) {
                    getQrCodesResultForEuropeanVaccination(
                        greenCardType = greenCardType,
                        credentials = credentials,
                        shouldDisclose = shouldDisclose,
                        qrCodeWidth = qrCodeWidth,
                        qrCodeHeight = qrCodeHeight
                    )
                } else {
                    getQrCodesResultForNonVaccination(
                        greenCardType = greenCardType,
                        credentials = credentials,
                        shouldDisclose = shouldDisclose,
                        qrCodeWidth = qrCodeWidth,
                        qrCodeHeight = qrCodeHeight
                    )
                }
            }
        }
    }

    private suspend fun getQrCodesResultForDomestic(
        greenCardType: GreenCardType,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult.SingleQrCode {
        val credential = credentials.first()
        val qrCodeBitmap = qrCodeUseCase.qrCode(
            credential = credential,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight,
            shouldDisclose = shouldDisclose,
            errorCorrectionLevel = greenCardUtil.getErrorCorrectionLevel(greenCardType)
        )

        return QrCodesResult.SingleQrCode(
            QrCodeData.Domestic(
                bitmap = qrCodeBitmap,
                readDomesticCredential = mobileCoreWrapper.readDomesticCredential(credential)
            )
        )
    }

    private suspend fun getQrCodesResultForEuropeanVaccination(
        greenCardType: GreenCardType,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult.MultipleQrCodes {
        val europeanVaccinationQrCodeDataList = mapToEuropeanVaccinations(
            credentials, qrCodeWidth, qrCodeHeight, shouldDisclose, greenCardType
        )
        return QrCodesResult.MultipleQrCodes(
            europeanVaccinationQrCodeDataList = europeanVaccinationQrCodeDataList,
            mostRelevantVaccinationIndex = multipleQrCodesUtil.getMostRelevantQrCodeIndex(
                europeanVaccinationQrCodeDataList
            )
        )
    }

    private suspend fun mapToEuropeanVaccinations(
        credentials: List<ByteArray>,
        qrCodeWidth: Int,
        qrCodeHeight: Int,
        shouldDisclose: Boolean,
        greenCardType: GreenCardType
    ): List<QrCodeData.European.Vaccination> {
        val readEuropeanCredentials =
            credentials.map { mobileCoreWrapper.readEuropeanCredential(it) }
        return credentials.mapIndexed { index, credential ->
            val qrCodeBitmap = qrCodeUseCase.qrCode(
                credential = credential,
                qrCodeWidth = qrCodeWidth,
                qrCodeHeight = qrCodeHeight,
                shouldDisclose = shouldDisclose,
                errorCorrectionLevel = greenCardUtil.getErrorCorrectionLevel(greenCardType)
            )

            val readEuropeanCredential = readEuropeanCredentials[index]
            val dose = readEuropeanCredentialUtil.getDose(readEuropeanCredential) ?: ""
            val totalDoses =
                readEuropeanCredentialUtil.getOfTotalDoses(readEuropeanCredential) ?: ""

            QrCodeData.European.Vaccination(
                dose = dose,
                ofTotalDoses = totalDoses,
                bitmap = qrCodeBitmap,
                readEuropeanCredential = readEuropeanCredential,
                isHidden = credentialUtil.vaccinationShouldBeHidden(readEuropeanCredentials, index)
            )
        }
    }

    private suspend fun getQrCodesResultForNonVaccination(
        greenCardType: GreenCardType,
        credentials: List<ByteArray>,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult.SingleQrCode {
        val credential = credentials.first()
        val qrCodeBitmap = qrCodeUseCase.qrCode(
            credential = credential,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight,
            shouldDisclose = shouldDisclose,
            errorCorrectionLevel = greenCardUtil.getErrorCorrectionLevel(greenCardType)
        )

        return QrCodesResult.SingleQrCode(
            QrCodeData.European.NonVaccination(
                bitmap = qrCodeBitmap,
                readEuropeanCredential = mobileCoreWrapper.readEuropeanCredential(credential)
            )
        )
    }
}