/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes.usecases

import android.graphics.Bitmap
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodeUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
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
    suspend fun qrCode(credential: ByteArray, shouldDisclose: QrCodeFragmentData.ShouldDisclose, qrCodeWidth: Int, qrCodeHeight: Int, errorCorrectionLevel: ErrorCorrectionLevel): Bitmap
}

class QrCodeUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val qrCodeUtil: QrCodeUtil,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val clockDeviationUseCase: ClockDeviationUseCase
) : QrCodeUseCase {

    override suspend fun qrCode(
        credential: ByteArray,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
        qrCodeWidth: Int,
        qrCodeHeight: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): Bitmap =
        withContext(Dispatchers.IO) {
            val qrCodeContent = when (shouldDisclose) {
                is QrCodeFragmentData.ShouldDisclose.Disclose -> {
                    val secretKey = holderDatabase.secretKeyDao().get(shouldDisclose.greenCardId).secretKey
                    mobileCoreWrapper.disclose(
                        secretKey.toByteArray(),
                        credential,
                        Clock.systemDefaultZone().millis() - clockDeviationUseCase.calculateServerTimeOffsetMillis(),
                        shouldDisclose.disclosurePolicy
                    )
                }
                is QrCodeFragmentData.ShouldDisclose.DoNotDisclose -> {
                    String(credential)
                }
            }

            qrCodeUtil.createQrCode(
                qrCodeContent = qrCodeContent,
                width = qrCodeWidth,
                height = qrCodeHeight,
                errorCorrectionLevel = errorCorrectionLevel
            )
        }
}
