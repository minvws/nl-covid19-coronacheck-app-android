/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import com.adevinta.android.barista.interaction.BaristaClickInteractions
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilViewIsShown

object QR {

    fun viewPreviousQR() {
        waitUntilViewIsShown(R.id.previousQrButton)
        BaristaClickInteractions.clickOn(R.id.previousQrButton)
    }
}
