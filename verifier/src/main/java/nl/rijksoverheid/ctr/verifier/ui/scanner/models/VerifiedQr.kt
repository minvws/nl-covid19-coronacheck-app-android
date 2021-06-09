package nl.rijksoverheid.ctr.verifier.ui.scanner.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import java.time.ZonedDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class VerifiedQr(
    val creationDateSeconds: Long,
    val testResultAttributes: TestResultAttributes
) : Parcelable {

    fun getDebugHtmlString(): String {
        return "<ul>" +
                "<li>Current date: ${ZonedDateTime.now()}</li>" +
                "<li>Creation date: ${creationDateSeconds}</li>" +
                "<li>isPaperProof: ${testResultAttributes.isNLDCC}</li>" +
                "<li>isSpecimen: ${testResultAttributes.isSpecimen}</li>" +
                "</ul>"
    }
}
