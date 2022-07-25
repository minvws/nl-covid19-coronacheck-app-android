/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.Context
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndState
import nl.rijksoverheid.ctr.shared.ext.getString

interface YourEventsEndStateUtil {
    fun getEndState(context: Context, hints: List<String>): YourEventsEndState
}

class YourEventsEndStateUtilImpl: YourEventsEndStateUtil {
    override fun getEndState(context: Context, hints: List<String>): YourEventsEndState {
        return if (hints.contains("negativetest_without_vaccinationassessment")) {
            return YourEventsEndState.AddVaccinationAssessment
        } else {
            val localisedHints = hints.map { context.getString(it) }
            if (localisedHints.isEmpty()) {
                YourEventsEndState.None
            } else {
                YourEventsEndState.Hints(localisedHints)
            }
        }
    }
}