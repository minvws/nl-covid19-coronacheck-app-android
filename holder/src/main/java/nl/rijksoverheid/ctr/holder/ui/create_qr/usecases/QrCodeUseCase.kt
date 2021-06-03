package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import android.graphics.Bitmap
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.QrCodeUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeUseCase {
    suspend fun qrCode(credential: ByteArray, shouldDisclose: Boolean, qrCodeWidth: Int, qrCodeHeight: Int, errorCorrectionLevel: ErrorCorrectionLevel): Bitmap
}

class QrCodeUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val qrCodeUtil: QrCodeUtil,
    private val mobileCoreWrapper: MobileCoreWrapper,
) : QrCodeUseCase {

    override suspend fun qrCode(
        credential: ByteArray,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): Bitmap =
        withContext(Dispatchers.IO) {
            val secretKey = persistenceManager.getSecretKeyJson()
                ?: throw IllegalStateException("Secret key should exist")

            val qrCodeContent = if (shouldDisclose) mobileCoreWrapper.disclose(
                secretKey.toByteArray(),
                credential
            ) else String(credential)

            qrCodeUtil.createQrCode(
                qrCodeContent = qrCodeContent,
                width = qrCodeWidth,
                height = qrCodeHeight,
                errorCorrectionLevel = errorCorrectionLevel
            )
        }
}
