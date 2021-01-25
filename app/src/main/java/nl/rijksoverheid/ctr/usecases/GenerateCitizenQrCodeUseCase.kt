package nl.rijksoverheid.ctr.usecases

import android.graphics.Bitmap
import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CustomerQr
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.crypto.CryptoUtil
import nl.rijksoverheid.ctr.data.models.EventQr
import nl.rijksoverheid.ctr.data.models.TestResults
import nl.rijksoverheid.ctr.qrcode.QrCodeTools
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GenerateCitizenQrCodeUseCase(
    private val cryptoUtil: CryptoUtil,
    private val moshi: Moshi,
    private val qrCodeTools: QrCodeTools
) {

    sealed class GenerateCitizenQrCodeResult {
        class Success(val bitmap: Bitmap) : GenerateCitizenQrCodeResult()
        class Failed(val reason: String) : GenerateCitizenQrCodeResult()
    }

    fun generateQrCode(
        event: EventQr.Event,
        validTestResult: TestResults.TestResult,
        validTestResultSignature: String,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): GenerateCitizenQrCodeResult {
        val keyPair = cryptoUtil.generatePublicAndPrivateKey()
            ?: return GenerateCitizenQrCodeResult.Failed("Failed to generate public and private key")
        val nonce = cryptoUtil.generateNonce()

        val payload = Payload(
            eventUuid = event.uuid,
            time = OffsetDateTime.now().toEpochSecond(),
            test = validTestResult,
            testSignature = validTestResultSignature
        )

        val newEncryptedPayloadBase64 = cryptoUtil.boxEasy(
            message = payload.toJson(moshi),
            nonceBytes = nonce,
            publicKeyBytes = Base64.decode(event.publicKey, Base64.NO_WRAP),
            privateKeyBytes = keyPair.second
        ) ?: return GenerateCitizenQrCodeResult.Failed("Failed to encrypt payload")


        val customerQR = CustomerQr(
            publicKey = Base64.encodeToString(keyPair.first, Base64.NO_WRAP),
            nonce = Base64.encodeToString(nonce, Base64.NO_WRAP),
            payload = newEncryptedPayloadBase64
        )

        return GenerateCitizenQrCodeResult.Success(
            qrCodeTools.createQrCode(
                customerQR.toJson(moshi),
                qrCodeWidth,
                qrCodeHeight
            )
        )
    }
}
