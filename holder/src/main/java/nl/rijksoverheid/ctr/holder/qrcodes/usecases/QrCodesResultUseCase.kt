/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes.usecases

import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.qrcodes.utils.MultipleQrCodesUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodesResult
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/**
 * Get all data needed to display QR codes based on data is send from the dashboard
 */
interface QrCodesResultUseCase {
    suspend fun getQrCodesResult(
        qrCodeFragmentData: QrCodeFragmentData,
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
    private val multipleQrCodesUtil: MultipleQrCodesUtil,
) : QrCodesResultUseCase {

    override suspend fun getQrCodesResult(
        qrCodeFragmentData: QrCodeFragmentData,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult {
        val greenCardType = qrCodeFragmentData.type
        val credentials = qrCodeFragmentData.credentials
        val shouldDisclose = qrCodeFragmentData.shouldDisclose
        val originType = qrCodeFragmentData.originType
        val credentialExpirationTimeSeconds = qrCodeFragmentData.credentialExpirationTimeSeconds

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
                        credentialExpirationTimeSeconds = credentialExpirationTimeSeconds,
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
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
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
                readDomesticCredential = mobileCoreWrapper.readDomesticCredential(credential),
            )
        )
    }

    private suspend fun getQrCodesResultForEuropeanVaccination(
        greenCardType: GreenCardType,
        credentials: List<ByteArray>,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
        credentialExpirationTimeSeconds: Long,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): QrCodesResult.MultipleQrCodes {
        val europeanVaccinationQrCodeDataList = mapToEuropeanVaccinations(
            credentialExpirationTimeSeconds, credentials, qrCodeWidth, qrCodeHeight, shouldDisclose, greenCardType
        )

        return QrCodesResult.MultipleQrCodes(
            europeanVaccinationQrCodeDataList = europeanVaccinationQrCodeDataList,
            mostRelevantVaccinationIndex = multipleQrCodesUtil.getMostRelevantQrCodeIndex(
                europeanVaccinationQrCodeDataList
            )
        )
    }

    private suspend fun mapToEuropeanVaccinations(
        credentialExpirationTimeSeconds: Long,
        credentials: List<ByteArray>,
        qrCodeWidth: Int,
        qrCodeHeight: Int,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
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

            val isExpired = credentialUtil.europeanCredentialHasExpired(credentialExpirationTimeSeconds)
            val isDoseSmaller = credentialUtil.vaccinationShouldBeHidden(readEuropeanCredentials, index)
            QrCodeData.European.Vaccination(
                dose = dose,
                ofTotalDoses = totalDoses,
                bitmap = qrCodeBitmap,
                readEuropeanCredential = readEuropeanCredential,
                isExpired = isExpired,
                isDoseNumberSmallerThanTotalDose = isDoseSmaller
            )
        }
    }

    private suspend fun getQrCodesResultForNonVaccination(
        greenCardType: GreenCardType,
        credentials: List<ByteArray>,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
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