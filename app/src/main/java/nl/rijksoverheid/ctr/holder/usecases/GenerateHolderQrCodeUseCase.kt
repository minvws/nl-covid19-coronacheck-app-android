package nl.rijksoverheid.ctr.holder.usecases

import android.graphics.Bitmap
import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.HolderQr
import nl.rijksoverheid.ctr.holder.models.HolderQrPayload
import nl.rijksoverheid.ctr.shared.models.AllowedTestResult
import nl.rijksoverheid.ctr.shared.models.Event
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import nl.rijksoverheid.ctr.shared.util.QrCodeUtils
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GenerateHolderQrCodeUseCase(
    private val cryptoUtil: CryptoUtil,
    private val moshi: Moshi,
    private val qrCodeUtils: QrCodeUtils
) {

    fun bitmap(
        event: Event,
        allowedTestResult: AllowedTestResult,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap {
        val keyPair = cryptoUtil.generatePublicAndPrivateKey()
            ?: throw Exception("Failed to generate public and private key")
        val nonce = cryptoUtil.generateNonce()

        val payload = HolderQrPayload(
            eventUuid = event.uuid,
            time = OffsetDateTime.now().toEpochSecond(),
            test = allowedTestResult.testResult,
            testSignature = allowedTestResult.testResultSignature.signature
        )

        val newEncryptedPayloadBase64 = cryptoUtil.boxEasy(
            message = payload.toJson(moshi),
            nonceBytes = nonce,
            publicKeyBytes = Base64.decode(event.publicKey, Base64.NO_WRAP),
            privateKeyBytes = keyPair.second
        ) ?: throw Exception("Failed to encrypt payload")


        val customerQR = HolderQr(
            publicKey = Base64.encodeToString(keyPair.first, Base64.NO_WRAP),
            nonce = Base64.encodeToString(nonce, Base64.NO_WRAP),
            payload = newEncryptedPayloadBase64
        )

        return qrCodeUtils.createQrCode(
            customerQR.toJson(moshi),
            qrCodeWidth,
            qrCodeHeight
        )
    }
}
