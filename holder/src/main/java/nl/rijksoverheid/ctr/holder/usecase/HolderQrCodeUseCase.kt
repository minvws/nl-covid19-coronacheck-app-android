package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import clmobile.Clmobile
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderQrCodeUseCase(
    private val eventRepository: EventRepository,
    private val holderRepository: HolderRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase,
    private val secretKeyUseCase: SecretKeyUseCase
) {

    suspend fun qrCode(accessToken: String, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap {
        val remoteNonce = holderRepository.remoteNonce()
        val commitmentMessage = commitmentMessageUseCase.json(
            nonce =
            remoteNonce.nonce
        )
        Timber.i("Received commitment message $commitmentMessage")

        val testIsmJson = eventRepository.testIsmJson(
            accessToken = accessToken,
            sToken = remoteNonce.sToken,
            icm = commitmentMessage
        )

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
