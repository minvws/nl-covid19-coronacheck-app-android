package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ClmobileWrapper

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface QrCodeUseCase {
    suspend fun qrCode(credentials: ByteArray, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap
}

class QrCodeUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase,
    private val clmobileWrapper: ClmobileWrapper
) : QrCodeUseCase {

    override suspend fun qrCode(
        credentials: ByteArray,
        qrCodeWidth: Int,
        qrCodeHeight: Int
    ): Bitmap =
        withContext(Dispatchers.IO) {
            val secretKey = persistenceManager.getSecretKeyJson()
                ?: throw IllegalStateException("Secret key should exist")

            val qrCodeContent = clmobileWrapper.discloseAllWithTimeQrEncoded(
                secretKey.toByteArray(),
                credentials
            )

            generateHolderQrCodeUseCase.bitmap(
                data = qrCodeContent,
                qrCodeWidth = qrCodeWidth,
                qrCodeHeight = qrCodeHeight
            )
        }
}
