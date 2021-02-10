package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import android.util.Base64
import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import org.json.JSONObject
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderQrCodeUseCase(
    private val moshi: Moshi,
    private val testProviderUseCase: TestProviderUseCase,
    private val holderRepository: HolderRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase,
    private val secretKeyUseCase: SecretKeyUseCase
) {

    suspend fun qrCode(accessToken: String, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap {
        // Hardcoded positive test result
        val positiveTestResult = JSONObject()
        positiveTestResult.put("token", "694B1DEAA702")
        positiveTestResult.put("protocolVersion", "1.0")
        positiveTestResult.put("providerIdentifier", "BRB")
        Timber.i("Received positive test result $positiveTestResult")

        val testProvider =
            testProviderUseCase.testProvider(positiveTestResult.getString("providerIdentifier"))
                ?: throw Exception("Unknown test provider") // TODO: Catch exception
        Timber.i("Received test provider $testProvider")

        val testResultJson = holderRepository.remoteTestResult(
            url = testProvider.resultUrl,
            token = positiveTestResult.getString("token"),
            verifierCode = ""
        ).toJson(moshi)
        Timber.i("Received test result json $testResultJson")

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
