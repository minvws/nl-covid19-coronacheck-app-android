/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.res.Resources
import android.text.TextUtils
import java.util.Locale
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase

interface TestInfoScreenUtil {

    fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean = true
    ): InfoScreen

    fun getForPositiveTest(
        event: RemoteEventPositiveTest,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen
}

class TestInfoScreenUtilImpl(
    private val resources: Resources,
    private val paperProofUtil: PaperProofUtil,
    private val countryUtil: CountryUtil,
    cachedAppConfigUseCase: HolderCachedAppConfigUseCase
) : TestInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String,
        europeanCredential: ByteArray?,
        addExplanation: Boolean
    ): InfoScreen {
        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == event.negativeTest?.type
        }?.name ?: event.negativeTest?.type ?: ""

        val isRat = event.negativeTest?.type == "LP217198-3"

        val testName = if (isRat) {
            holderConfig.euTestNames.firstOrNull {
                it.code == event.negativeTest?.manufacturer
            }?.name ?: ""
        } else {
            event.negativeTest?.name ?: ""
        }

        val testLocation = event.negativeTest?.facility ?: ""

        val testManufacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == event.negativeTest?.manufacturer
            }?.name ?: event.negativeTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

        val country = getCountry(event.negativeTest?.country)

        val title =
            if (europeanCredential != null) resources.getString(R.string.your_vaccination_explanation_toolbar_title) else resources.getString(
                R.string.your_test_result_explanation_toolbar_title
            )
        val header = if (europeanCredential != null) {
            resources.getString(R.string.paper_proof_event_explanation_header)
        } else {
            resources.getString(R.string.your_test_result_explanation_description_header)
        }

        val description = (TextUtils.concat(
            header,
            "<br/><br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_name),
                fullName
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_date_of_birth),
                birthDate,
                isOptional = true
            ),
            "<br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_type),
                testType,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_name),
                testName,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_date),
                testDate,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_result),
                resources.getString(R.string.your_test_result_explanation_negative_test_result),
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_manufacturer),
                testManufacturer,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_location),
                testLocation,
                isOptional = true
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
                resources.getString(R.string.your_test_result_explanation_description_unique_identifier),
                unique
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

    private fun getCountry(country: String?) = when {
        country.isNullOrEmpty() -> "NL"
        else -> country
    }

    override fun getForPositiveTest(
        event: RemoteEventPositiveTest,
        testDate: String,
        fullName: String,
        birthDate: String
    ): InfoScreen {

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == event.positiveTest?.type
        }?.name ?: event.positiveTest?.type ?: ""

        val testName = event.positiveTest?.name ?: ""

        val testLocation = event.positiveTest?.facility ?: ""

        val testManufacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == event.positiveTest?.manufacturer
            }?.name ?: event.positiveTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

        val country = getCountry(event.positiveTest?.country)

        val title = resources.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = (TextUtils.concat(
            resources.getString(R.string.your_test_result_explanation_description_header),
            "<br/><br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_name),
                fullName
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_date_of_birth),
                birthDate,
                isOptional = true
            ),
            "<br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_type),
                testType,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_name),
                testName,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_date),
                testDate,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_result),
                resources.getString(R.string.your_test_result_explanation_positive_test_result),
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_manufacturer),
                testManufacturer,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_location),
                testLocation,
                isOptional = true
            ),
            createdLine(
                resources.getString(R.string.holder_event_about_test_countrytestedin),
                countryUtil.getCountryForInfoScreen(Locale.getDefault().language, country),
                isOptional = true
            ),
            "<br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_unique_identifier),
                unique
            )
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
