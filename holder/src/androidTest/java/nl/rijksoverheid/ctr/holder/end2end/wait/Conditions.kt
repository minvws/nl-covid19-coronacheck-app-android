/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.wait

import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewInteraction
import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.getView

class ButtonInState(private val viewInteraction: ViewInteraction, private val enabled: Boolean) : Condition() {
    override val description = "view is " + if (enabled) "enabled" else "disabled"

    override fun checkCondition(): Boolean? {
        return try {
            viewInteraction.getView()?.isEnabled == enabled
        } catch (e: NoMatchingViewException) {
            null
        }
    }
}

class ViewIsShown(private val viewInteraction: ViewInteraction, private val shown: Boolean) : Condition() {
    override val description = "text is shown"

    override fun checkCondition(): Boolean? {
        return try {
            viewInteraction.getView()?.isShown == shown
        } catch (e: NoMatchingViewException) {
            null
        }
    }
}
