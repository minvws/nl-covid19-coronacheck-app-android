package nl.rijksoverheid.ctr.holder.fuzzy_matching

import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent.Companion.TYPE_NEGATIVE_TEST
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent.Companion.TYPE_POSITIVE_TEST
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent.Companion.TYPE_RECOVERY
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent.Companion.TYPE_TEST
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent.Companion.TYPE_VACCINATION
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventStringUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SelectionDataUtil {
    fun events(remoteEvents: List<RemoteEvent>): String
    fun details(
        providerIdentifier: String,
        remoteEvents: List<RemoteEvent>
    ): List<SelectionDetailData>
}

class SelectionDataUtilImpl(
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val yourEventsFragmentUtil: YourEventsFragmentUtil,
    private val remoteEventStringUtil: RemoteEventStringUtil,
    private val getQuantityString: (Int, Int) -> String,
    private val getFormattedString: (Int, String) -> String,
    private val getString: (Int) -> String
) : SelectionDataUtil {

    override fun events(remoteEvents: List<RemoteEvent>): String {
        val testResultTypes = listOf(TYPE_NEGATIVE_TEST, TYPE_POSITIVE_TEST, TYPE_RECOVERY, TYPE_TEST)
        val vaccinationCount = remoteEvents.filter { it.type == TYPE_VACCINATION }.size
        val testCount = remoteEvents.filter { testResultTypes.contains(it.type) }.size

        val eventsString = StringBuilder()

        if (vaccinationCount > 0) {
            eventsString.append(
                "$vaccinationCount ${
                    getQuantityString(
                        R.plurals.general_vaccinations,
                        vaccinationCount
                    )
                }"
            )
        }
        if (testCount > 0) {
            if (eventsString.isNotEmpty()) {
                eventsString.append(" ${getString(R.string.general_and)} ")
            }
            eventsString.append(
                "$testCount ${
                    getQuantityString(
                        R.plurals.general_testresults,
                        testCount
                    )
                }"
            )
        }

        return eventsString.toString()
    }

    override fun details(
        providerIdentifier: String,
        remoteEvents: List<RemoteEvent>
    ): List<SelectionDetailData> {
        val configProviders = cachedAppConfigUseCase.getCachedAppConfig().providers

        val data = remoteEvents.map { event ->
            val eventDate = event.getDate()
            SelectionDetailData(
                type = "${remoteEventStringUtil.remoteEventTitle(event.javaClass)}${
                    if (providerIdentifier.startsWith("dcc") && event is RemoteEventVaccination) {
                        val dose = event.vaccination?.doseNumber
                        val totalDoses = event.vaccination?.totalDoses
                        if (dose != null && totalDoses != null) {
                            " ${getFormattedString(R.string.your_vaccination_explanation_dose, "$dose/$totalDoses")}"
                        } else {
                            ""
                        }
                    } else {
                        ""
                    }
                }",
                providerIdentifiers = listOf(
                    yourEventsFragmentUtil.getProviderName(
                        configProviders,
                        providerIdentifier
                    )
                ),
                eventDate = eventDate?.toLocalDate()?.formatDayMonthYear() ?: ""
            )
        }

        return data
    }
}
