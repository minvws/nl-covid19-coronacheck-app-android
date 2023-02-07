/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.scrollListToPosition
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.card
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapButton
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.EventType

object Overview {

    fun scrollToBottomOfOverview() {
        waitUntilViewIsShown(R.id.recyclerView)
        for (i in 2 until 12 step 2) scrollListToPosition(R.id.recyclerView, i)
    }

    fun viewQR(eventType: EventType) {
        scrollToBottomOfOverview()
        card(eventType).tapButton("Bekijk QR")
        waitUntilTextIsShown("Internationale QR")
    }
}
