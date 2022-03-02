/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.YourEventsFragmentType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

interface YourEventsFragmentUtil {
    fun getNoOriginTypeCopy(type: YourEventsFragmentType): Int
    fun getProviderName(type: YourEventsFragmentType, providerIdentifier: String): String
    fun getCancelDialogDescription(type: YourEventsFragmentType): Int
    fun getFullName(holder: RemoteProtocol3.Holder?): String
    fun getBirthDate(holder: RemoteProtocol3.Holder?): String
}

class YourEventsFragmentUtilImpl(
    private val remoteEventUtil: RemoteEventUtil
): YourEventsFragmentUtil {

    override fun getNoOriginTypeCopy(type: YourEventsFragmentType): Int {
        return when (type) {
            is YourEventsFragmentType.TestResult2 -> {
                R.string.rule_engine_no_test_origin_description_negative_test
            }
            is YourEventsFragmentType.DCC -> {
                R.string.rule_engine_no_test_origin_description_scanned_qr_code
            }
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                return when (remoteEventUtil.getOriginType(type.remoteEvents.keys.first().events!!.first())) {
                    is OriginType.Test -> {
                        R.string.rule_engine_no_test_origin_description_negative_test
                    }
                    is OriginType.Recovery -> {
                        R.string.rule_engine_no_test_origin_description_positive_test
                    }
                    is OriginType.Vaccination -> {
                        R.string.rule_engine_no_test_origin_description_vaccination
                    }
                    is OriginType.VaccinationAssessment -> {
                        R.string.general_vaccinationAssessment
                    }
                }
            }
        }
    }

    override fun getProviderName(type: YourEventsFragmentType, providerIdentifier: String): String {
        return (type as? YourEventsFragmentType.RemoteProtocol3Type)
            ?.eventProviders?.firstOrNull { it.identifier == providerIdentifier }
            ?.name
            ?: providerIdentifier
    }

    override fun getCancelDialogDescription(type: YourEventsFragmentType): Int {
        return when (type) {
            is YourEventsFragmentType.DCC -> R.string.holder_dcc_alert_message
            is YourEventsFragmentType.TestResult2 -> R.string.holder_test_alert_message
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                when (remoteEventUtil.getOriginType(type.remoteEvents.keys.first().events!!.first())) {
                    is OriginType.Test -> R.string.holder_test_alert_message
                    is OriginType.Recovery -> R.string.holder_recovery_alert_message
                    is OriginType.Vaccination -> R.string.holder_vaccination_alert_message
                    is OriginType.VaccinationAssessment -> R.string.holder_event_vaccination_assessment_alert_message
                }
            }
        }
    }

    override fun getFullName(holder: RemoteProtocol3.Holder?): String {
        return holder?.let {
            return if (it.infix.isNullOrEmpty()) {
                "${it.lastName}, ${it.firstName}"
            } else {
                "${it.infix} ${it.lastName}, ${it.firstName}"
            }
        } ?: ""
    }

    override fun getBirthDate(holder: RemoteProtocol3.Holder?): String {
        return holder?.birthDate?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: DateTimeParseException) {
                // Check if date has removed content, if so return string directly
                if (birthDate.contains("XX")) {
                    birthDate
                } else ""
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }
}