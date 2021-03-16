package nl.rijksoverheid.ctr.verifier.usecases

import clmobile.Clmobile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.verifier.models.DecryptedQr
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
class DecryptHolderQrUseCase(
    private val moshi: Moshi
) {

    suspend fun decrypt(
        content: String
    ): DecryptResult = withContext(Dispatchers.IO) {
        val result =
            Clmobile.verifyQREncoded(
                content.toByteArray()
            )
                .verify()
        Timber.i("QR Code created at ${result.unixTimeSeconds}")
        val testResultAttributes =
            result.attributesJson.decodeToString()
                .toObject<TestResultAttributes>(moshi)
        DecryptResult.Success(
            DecryptedQr(
                creationDate = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(result.unixTimeSeconds),
                    ZoneOffset.UTC
                ),
                sampleDate = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(testResultAttributes.sampleTime),
                    ZoneOffset.UTC
                ),
                testType = testResultAttributes.testType,
                firstNameInitial = testResultAttributes.firstNameInitial,
                lastNameInitial = testResultAttributes.lastNameInitial,
                birthDay = testResultAttributes.birthDay,
                birthMonth = testResultAttributes.birthMonth,
                isPaperProof = testResultAttributes.isPaperProof
            )
        )
    }

    sealed class DecryptResult {
        class Success(val decryptQr: DecryptedQr) : DecryptResult()
        object Failed : DecryptResult()
    }
}
