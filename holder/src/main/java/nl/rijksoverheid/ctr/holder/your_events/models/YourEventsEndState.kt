/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.models

sealed class YourEventsEndState {
    object None: YourEventsEndState()
    object AddVaccinationAssessment: YourEventsEndState()
    data class Hints(val localisedHints: List<String>): YourEventsEndState()
}