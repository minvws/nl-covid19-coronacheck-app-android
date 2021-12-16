package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import android.graphics.Bitmap
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.QrCodeUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import java.time.Clock

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
    clock: Clock,
    clockDeviationUseCase: ClockDeviationUseCase
) : QrCodeUseCase {

    private val adjustedCurrentMillis: Long = clock.millis() - clockDeviationUseCase.calculateServerTimeOffsetMillis()

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
                credential,
                adjustedCurrentMillis
            ) else String(credential)

            qrCodeUtil.createQrCode(
                qrCodeContent = qrCodeContent,
                width = qrCodeWidth,
                height = qrCodeHeight,
                errorCorrectionLevel = errorCorrectionLevel
            )
        }
}
