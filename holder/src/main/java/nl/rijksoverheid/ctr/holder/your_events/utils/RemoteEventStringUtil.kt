package nl.rijksoverheid.ctr.holder.your_events.utils

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.shared.ext.capitalize

/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

interface RemoteEventStringUtil {
    fun remoteEventTitle(remoteEventClass: Class<out RemoteEvent>): String
}

class RemoteEventStringUtilImpl(
    private val getString: (Int) -> String
) : RemoteEventStringUtil {
    override fun remoteEventTitle(remoteEventClass: Class<out RemoteEvent>): String {
        return when (remoteEventClass) {
            RemoteEventVaccination::class.java -> getString(R.string.general_vaccination)
            RemoteEventVaccinationAssessment::class.java -> getString(R.string.general_visitorPass)
            RemoteEventNegativeTest::class.java -> getString(R.string.general_negativeTest)
            RemoteEventPositiveTest::class.java -> getString(R.string.general_positiveTest)
            RemoteEventRecovery::class.java -> getString(R.string.general_recoverycertificate)
            else -> ""
        }.capitalize()
    }
}
