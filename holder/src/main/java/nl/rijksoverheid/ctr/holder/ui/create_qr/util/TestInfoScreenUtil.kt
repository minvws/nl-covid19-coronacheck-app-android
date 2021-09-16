/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import android.content.res.Resources
import android.text.TextUtils
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.shared.models.PersonalDetails

interface TestInfoScreenUtil {

    fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String
    ): InfoScreen

    fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String
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
    cachedAppConfigUseCase: CachedAppConfigUseCase
) : TestInfoScreenUtil {

    private val holderConfig = cachedAppConfigUseCase.getCachedAppConfig()

    override fun getForRemoteTestResult2(
        result: RemoteTestResult2.Result,
        personalDetails: PersonalDetails,
        testDate: String
    ): InfoScreen {
        val title = resources.getString(R.string.your_test_result_explanation_toolbar_title)
        val description = (TextUtils.concat(
            resources.getString(R.string.your_test_result_explanation_description_header),
            "<br/><br/>",
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_your_details),
                "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_type),
                result.testType,
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_date),
                testDate
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_result),
                resources.getString(R.string.your_test_result_explanation_negative_test_result)
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_unique_identifier),
                result.unique
            )
        ) as String)

        return InfoScreen(
            title = title,
            description = description
        )
    }

    override fun getForNegativeTest(
        event: RemoteEventNegativeTest,
        fullName: String,
        testDate: String,
        birthDate: String
    ): InfoScreen {

        val testType = holderConfig.euTestTypes.firstOrNull {
            it.code == event.negativeTest?.type
        }?.name ?: event.negativeTest?.type ?: ""

        val testName = event.negativeTest?.name ?: ""

        val testLocation = event.negativeTest?.facility ?: ""

        val testManufacturer =
            holderConfig.euTestManufacturers.firstOrNull {
                it.code == event.negativeTest?.manufacturer
            }?.name ?: event.negativeTest?.manufacturer ?: ""

        val unique = event.unique ?: ""

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
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_type),
                testType
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_name),
                testName
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_date),
                testDate
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_result),
                resources.getString(R.string.your_test_result_explanation_negative_test_result)
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_location),
                testLocation
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_manufacturer),
                testManufacturer
            ),
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
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_type),
                testType
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_name),
                testName
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_date),
                testDate
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_result),
                resources.getString(R.string.your_test_result_explanation_positive_test_result)
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_location),
                testLocation
            ),
            createdLine(
                resources.getString(R.string.your_test_result_explanation_description_test_manufacturer),
                testManufacturer
            ),
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