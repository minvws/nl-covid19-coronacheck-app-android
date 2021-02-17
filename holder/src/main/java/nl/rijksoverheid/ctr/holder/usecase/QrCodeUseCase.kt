package nl.rijksoverheid.ctr.holder.usecase

import android.graphics.Bitmap
import clmobile.Clmobile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.util.CryptoUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeUseCase(
    private val persistenceManager: PersistenceManager,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase,
) {

    suspend fun qrCode(credentials: ByteArray, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap =
        withContext(Dispatchers.IO) {
            val secretKey = persistenceManager.getSecretKeyJson()
                ?: throw IllegalStateException("Secret key should exist")

            val qrCodeContent = Clmobile.discloseAllWithTimeQrEncoded(
                CryptoUtil.ISSUER_PK_XML.toByteArray(),
                secretKey.toByteArray(),
                credentials
            ).successString()

            generateHolderQrCodeUseCase.bitmap(
                data = qrCodeContent,
                qrCodeWidth = qrCodeWidth,
                qrCodeHeight = qrCodeHeight
            )
        }
}
