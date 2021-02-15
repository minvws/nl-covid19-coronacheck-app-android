package nl.rijksoverheid.ctr.verifier.usecases

import android.util.Base64
import clmobile.Clmobile
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.DecryptedQr
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.util.CryptoUtil
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DecryptHolderQrUseCase(private val moshi: Moshi) {

    fun decrypt(
        content: String
    ): DecryptedQr {
        val decodedContent = Base64.decode(content.toByteArray(), Base64.DEFAULT)
        val result =
            Clmobile.verify(CryptoUtil.ISSUER_PK_XML.toByteArray(), decodedContent).verify()
        Timber.i("QR Code created at ${result.unixTimeSeconds}")
        val testResultAttributes =
            result.attributesJson.decodeToString().toObject<TestResultAttributes>(moshi)
        return DecryptedQr(
            creationDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(result.unixTimeSeconds),
                ZoneOffset.UTC
            ),
            sampleDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(testResultAttributes.sampleTime),
                ZoneOffset.UTC
            ),
            testType = testResultAttributes.testType
        )
    }
}
