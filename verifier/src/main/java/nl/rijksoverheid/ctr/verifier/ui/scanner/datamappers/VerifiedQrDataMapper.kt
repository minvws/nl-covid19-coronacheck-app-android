package nl.rijksoverheid.ctr.verifier.ui.scanner.datamappers

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.toObject
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

class VerifiedQrDataMapperImpl(private val moshi: Moshi, private val mobileCoreWrapper: MobileCoreWrapper) : VerifiedQrDataMapper {
    override fun transform(
        qrContent: String
    ): VerifiedQr {

        val result =
            mobileCoreWrapper.verify(
                qrContent.toByteArray()
            ).verify()

        return VerifiedQr(
            creationDateSeconds = 0,
            testResultAttributes = result.decodeToString().toObject(moshi))
    }
}
