package nl.rijksoverheid.ctr.usecases

import android.graphics.Bitmap
import android.util.Base64
import com.goterl.lazycode.lazysodium.LazySodiumAndroid
import com.goterl.lazycode.lazysodium.interfaces.SecretBox
import com.goterl.lazycode.lazysodium.utils.KeyPair
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.citizen.models.CustomerQr
import nl.rijksoverheid.ctr.citizen.models.Payload
import nl.rijksoverheid.ctr.data.models.EventQr
import nl.rijksoverheid.ctr.data.models.TestResults
import nl.rijksoverheid.ctr.factories.KeyFactory
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
    private val lazySodium: LazySodiumAndroid,
    private val moshi: Moshi,
    private val qrCodeTools: QrCodeTools
) {

    fun generateQrCode(
        event: EventQr.Event,
        validTestResult: TestResults.TestResult,
        validTestResultSignature: String,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap {
        val keyPair = lazySodium.cryptoBoxKeypair()
        val nonce = lazySodium.nonce(SecretBox.NONCEBYTES)

        val payload = Payload(
            eventUuid = event.uuid,
            time = OffsetDateTime.now().toEpochSecond(),
            test = validTestResult,
            testSignature = validTestResultSignature
        )

        val encryptedPayloadBase64 = lazySodium.cryptoBoxEasy(
            payload.toJson(moshi),
            nonce,
            KeyPair(
                KeyFactory.createKeyFromBase64String(event.publicKey),
                keyPair.secretKey
            )
        )

        val customerQR = CustomerQr(
            publicKey = Base64.encodeToString(keyPair.publicKey.asBytes, Base64.NO_WRAP),
            nonce = Base64.encodeToString(nonce, Base64.NO_WRAP),
            payload = encryptedPayloadBase64
        )

        return qrCodeTools.createQrCode(customerQR.toJson(moshi), qrCodeWidth, qrCodeHeight)
    }
}
