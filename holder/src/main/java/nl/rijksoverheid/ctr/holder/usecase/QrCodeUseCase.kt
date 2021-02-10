package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import android.util.Base64
import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeUseCase(
    private val moshi: Moshi,
    private val holderRepository: HolderRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase,
    private val secretKeyUseCase: SecretKeyUseCase
) {

    suspend fun qrCode(testResult: RemoteTestResult, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap {
        val testResultJson = testResult.toJson(moshi)

        val remoteNonce = holderRepository.remoteNonce()
        val commitmentMessage = commitmentMessageUseCase.json(
            nonce =
            remoteNonce.nonce
        )
        Timber.i("Received commitment message $commitmentMessage")

        val testIsmJson = holderRepository.testIsmJson(
            test = testResultJson,
            sToken = remoteNonce.sToken,
            icm = commitmentMessage
        )
        Timber.i("Received test ism json $testIsmJson")

        val credentials = Clmobile.createCredential(
            secretKeyUseCase.json().toByteArray(),
            testIsmJson.toByteArray()
        ).verify()

        return qrCode(
            credentials = credentials,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight
        )
    }

    suspend fun qrCode(credentials: ByteArray, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap {
        val proof = Clmobile.discloseAllWithTime(
            CryptoUtil.ISSUER_PK_XML.toByteArray(),
            credentials
        ).verify()

        val base64Qr = Base64.encodeToString(proof, Base64.NO_WRAP)

        return generateHolderQrCodeUseCase.bitmap(
            data = base64Qr,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight
        )
    }
}
