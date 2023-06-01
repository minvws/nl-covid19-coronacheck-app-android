/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import androidx.core.text.HtmlCompat
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
    fun getHeaderCopy(type: YourEventsFragmentType, flow: Flow): Int
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
    override fun getHeaderCopy(type: YourEventsFragmentType, flow: Flow): Int {
        return when {
            flow == HolderFlow.Migration -> {
                R.string.holder_migrationFlow_scannedDetailsOverview_message
            }
            type is YourEventsFragmentType.DCC -> {
                R.string.holder_listRemoteEvents_paperflow_message
            }
            isRecovery(type) -> {
                R.string.holder_listRemoteEvents_recovery_message
            }
            isTest(type) -> {
                R.string.holder_listRemoteEvents_negativeTest_message
            }
            else -> {
                R.string.holder_listRemoteEvents_vaccination_message
            }
        }
    }

    private fun isRecovery(yourEventsFragmentType: YourEventsFragmentType): Boolean {
        val type =
            yourEventsFragmentType as? YourEventsFragmentType.RemoteProtocol3Type ?: return false
        val remoteEvent = type.remoteEvents.keys.firstOrNull()?.events?.first() ?: return false
        return remoteEventUtil.getOriginType(remoteEvent) == OriginType.Recovery
    }

    private fun isTest(yourEventsFragmentType: YourEventsFragmentType): Boolean {
        val type =
            yourEventsFragmentType as? YourEventsFragmentType.RemoteProtocol3Type ?: return false
        val remoteEvent = type.remoteEvents.keys.firstOrNull()?.events?.first() ?: return false
        return remoteEventUtil.getOriginType(remoteEvent) == OriginType.Test
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
                            R.string.general_retrievedDetails
                        } else {
                            R.string.rule_engine_no_test_origin_description_vaccination
                        }
                    }
                }
            }
        }
    }

    override fun getProviderName(
        providers: List<AppConfig.Code>,
        providerIdentifier: String
    ): String {
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
                }
            }
        }
    }

    override fun getFullName(holder: RemoteProtocol.Holder?): String {
        return holder?.let {
            return HtmlCompat.fromHtml(
                if (it.infix.isNullOrEmpty()) {
                    "${it.lastName}, ${it.firstName}"
                } else {
                    "${it.infix} ${it.lastName}, ${it.firstName}"
                }, HtmlCompat.FROM_HTML_MODE_LEGACY
            ).toString()
        } ?: ""
    }

    override fun getBirthDate(holder: RemoteProtocol.Holder?): String {
        return holder?.birthDate?.let {
            val birthDate = HtmlCompat.fromHtml(it, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
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

    override fun getVaccinationDate(date: LocalDate?): String {
        return date?.let { vaccinationDate ->
            try {
                vaccinationDate.formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""
    }
}
