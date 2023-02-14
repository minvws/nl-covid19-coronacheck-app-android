/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertOverview
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickListItemChild
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.scrollListToPosition
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilViewIsShown

object Overview {

    fun scrollToBottomOfOverview(position: Int) {
        waitUntilViewIsShown(R.id.recyclerView)
        scrollListToPosition(R.id.recyclerView, position + 3)
    }

    fun viewQR(position: Int = 0) {
        scrollToBottomOfOverview(position)
        clickListItemChild(R.id.recyclerView, position + 2, R.id.button)
        waitUntilTextIsShown("Internationale QR")
    }

    fun backToOverview() {
        clickBack()
        assertOverview()
    }
}
