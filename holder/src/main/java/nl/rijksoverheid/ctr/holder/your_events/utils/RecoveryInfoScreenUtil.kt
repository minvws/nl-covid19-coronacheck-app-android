/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.res.Resources
import android.text.TextUtils
import java.util.Locale
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtil

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
    private val paperProofUtil: PaperProofUtil,
    private val countryUtil: CountryUtil
) : CreateInfoLineUtil(), RecoveryInfoScreenUtil {

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

        val isPaperCertificate = europeanCredential != null

        val title =
            if (europeanCredential != null) resources.getString(R.string.your_vaccination_explanation_toolbar_title) else resources.getString(
                R.string.your_test_result_explanation_toolbar_title
            )
        val header = if (europeanCredential != null) {
            resources.getString(R.string.paper_proof_event_explanation_header)
        } else {
            resources.getString(R.string.recovery_explanation_description_header)
        }

        val countryValue = event.recovery?.country
        val country = when {
            countryValue.isNullOrEmpty() -> "NL"
            else -> countryValue
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
                resources.getString(R.string.holder_event_about_test_countrytestedin),
                countryUtil.getCountryForInfoScreen(Locale.getDefault().language, country),
                isOptional = true
            ),
            if (europeanCredential != null) {
                val issuerAnswer = paperProofUtil.getIssuer(europeanCredential)
                createdLine(
                    resources.getString(R.string.holder_dcc_issuer),
                    if (issuerAnswer == "Ministry of Health Welfare and Sport") {
                        resources.getString(R.string.qr_explanation_certificate_issuer)
                    } else {
                        issuerAnswer
                    },
                    isOptional = true
                )
            } else {
                ""
            },
            "<br/>",
            createdLine(
                resources.getString(R.string.recovery_explanation_description_valid_from),
                validFromDate
            ),
            createdLine(
                resources.getString(R.string.recovery_explanation_description_valid_until),
                validUntilDate
            ),
            "<br/>",
            createdLine(
                resources.getString(
                    if (isPaperCertificate) {
                        R.string.holder_dcc_test_identifier
                    } else {
                        R.string.your_test_result_explanation_description_unique_identifier
                    }
                ),
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
}
