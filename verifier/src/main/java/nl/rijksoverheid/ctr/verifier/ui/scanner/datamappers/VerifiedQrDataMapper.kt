package nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.VerifiedQr

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface VerifiedQrDataMapper {
    fun transform(qrContent: String): VerifiedQr
}

//TODO to be fixed in the next ticket
class VerifiedQrDataMapperImpl(private val moshi: Moshi, private val mobileCoreWrapper: MobileCoreWrapper) : VerifiedQrDataMapper {
    override fun transform(
        qrContent: String
    ): VerifiedQr {
        val mobileLibraryResult = mobileCoreWrapper.verify(
            qrContent.toByteArray()
        )

        val successfulResult = mobileLibraryResult.verify()

        //TODO check the structure and use an appropriate data class
        println("GIO ${mobileLibraryResult.value.decodeToString()}")

//        mobileLibraryResult.value.decodeToString().toObject(moshi)

        val result =
            mobileCoreWrapper.verify(
                qrContent.toByteArray()
            ).verify()

//        return VerifiedQr(
//            creationDateSeconds = result.unixTimeSeconds,
//            testResultAttributes = result.attributesJson.decodeToString().toObject(moshi)
//        )
        return VerifiedQr(
            creationDateSeconds = 0,
            testResultAttributes = TestResultAttributes(
                sampleTime = 0,
                testType = "dummy",
                birthDay =  "dummy",
                birthMonth =  "dummy",
                firstNameInitial =  "dummy",
                lastNameInitial =  "dummy",
                isPaperProof =  "dummy",
                isSpecimen =  "dummy"
            ))
    }
}
