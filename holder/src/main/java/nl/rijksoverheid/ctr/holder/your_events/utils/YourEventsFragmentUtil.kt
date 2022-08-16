/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.Flow

interface YourEventsFragmentUtil {
    fun getHeaderCopy(type: YourEventsFragmentType): Int
    fun getNoOriginTypeCopy(type: YourEventsFragmentType, flow: Flow): Int
    fun getProviderName(providers: List<AppConfig.Code>, providerIdentifier: String): String
    fun getCancelDialogDescription(type: YourEventsFragmentType): Int
    fun getFullName(holder: RemoteProtocol.Holder?): String
    fun getBirthDate(holder: RemoteProtocol.Holder?): String
    fun getVaccinationDate(date: LocalDate?): String
}

class YourEventsFragmentUtilImpl(
    private val remoteEventUtil: RemoteEventUtil
) : YourEventsFragmentUtil {
    override fun getHeaderCopy(type: YourEventsFragmentType): Int {
        return when (type) {
            is YourEventsFragmentType.DCC -> {
                R.string.holder_listRemoteEvents_paperflow_message
            }
            else -> {
                R.string.holder_listRemoteEvents_vaccination_message
            }
        }
    }

    override fun getNoOriginTypeCopy(type: YourEventsFragmentType, flow: Flow): Int {
        return when (type) {
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
                        if (flow is HolderFlow.VaccinationAndPositiveTest) {
                            R.string.about_this_app
                        } else {
                            R.string.rule_engine_no_test_origin_description_vaccination
                        }
                    }
                    is OriginType.VaccinationAssessment -> {
                        R.string.about_this_app
                    }
                }
            }
        }
    }

    override fun getProviderName(providers: List<AppConfig.Code>, providerIdentifier: String): String {
        return providers.firstOrNull { it.code == providerIdentifier }
            ?.name
            ?: providerIdentifier
    }

    override fun getCancelDialogDescription(type: YourEventsFragmentType): Int {
        return when (type) {
            is YourEventsFragmentType.DCC -> R.string.holder_dcc_alert_message
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

    override fun getFullName(holder: RemoteProtocol.Holder?): String {
        return holder?.let {
            return if (it.infix.isNullOrEmpty()) {
                "${it.lastName}, ${it.firstName}"
            } else {
                "${it.infix} ${it.lastName}, ${it.firstName}"
            }
        } ?: ""
    }

    override fun getBirthDate(holder: RemoteProtocol.Holder?): String {
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

    override fun getVaccinationDate(vaccinationDate: LocalDate?): String {
        return vaccinationDate?.let { date ->
            try {
                date.formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }
}
