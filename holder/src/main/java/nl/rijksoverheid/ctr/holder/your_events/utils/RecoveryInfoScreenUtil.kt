/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.res.Resources
import android.text.TextUtils
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper

interface RecoveryInfoScreenUtil {

    fun getForRecovery(
        event: RemoteEventRecovery,
        testDate: String,
        fullName: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
    ): InfoScreen
}

class RecoveryInfoScreenUtilImpl(
    val resources: Resources,
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val paperProofUtil: PaperProofUtil
) : RecoveryInfoScreenUtil {

    override fun getForRecovery(
        event: RemoteEventRecovery,
        testDate: String,
        fullName: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean
    ): InfoScreen {

        val validFromDate = event.recovery?.validFrom?.formatDayMonthYear() ?: ""
        val validUntilDate = event.recovery?.validUntil?.formatDayMonthYear() ?: ""

        val title = if (europeanCredential != null) resources.getString(R.string.your_vaccination_explanation_toolbar_title) else resources.getString(R.string.your_test_result_explanation_toolbar_title)
        val header = if (europeanCredential != null) {
            resources.getString(R.string.paper_proof_event_explanation_header)
        } else {
            resources.getString(R.string.recovery_explanation_description_header)
        }

        val description = (TextUtils.concat(
            header,
            "<br/><br/>",
            createdLine(
                resources.getString(R.string.recovery_explanation_description_name),
                fullName
            ),
            createdLine(
                resources.getString(R.string.recovery_explanation_description_birth_date),
                birthDate,
                isOptional = true
            ),
            "<br/>",
            createdLine(
                resources.getString(R.string.recovery_explanation_description_test_date),
                testDate
            ),
            createdLine(
                resources.getString(R.string.recovery_explanation_description_valid_from),
                validFromDate
            ),
            createdLine(
                resources.getString(R.string.recovery_explanation_description_valid_until),
                validUntilDate
            ),
            if (europeanCredential != null) {
                val issuerAnswer = paperProofUtil.getIssuer(europeanCredential)
                createdLine(resources.getString(R.string.holder_dcc_issuer), issuerAnswer, isOptional = true)
            } else {
                ""
            },
            createdLine(
                resources.getString(R.string.holder_event_about_test_countrytestedin),
                "Nederland"
            ),
            "<br/>",
            createdLine(
                resources.getString(R.string.recovery_explanation_description_unique_test_identifier),
                event.unique
            ),
            if (europeanCredential != null && addExplanation) {
                paperProofUtil.getInfoScreenFooterText(europeanCredential)
            } else {
                ""
            }
        ) as String)

        return InfoScreen(
            title = title,
            description = description
        )
    }

    private fun createdLine(
        name: String,
        nameAnswer: String,
        isOptional: Boolean = false
    ): String {
        return if (isOptional && nameAnswer.isEmpty()) "" else "$name <b>$nameAnswer</b><br/>"
    }
}
