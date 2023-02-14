/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.interaction.wait

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

class ButtonInState(private val viewInteraction: ViewInteraction, private val enabled: Boolean) :
    Condition() {

    override val description = "view is " + if (enabled) "enabled" else "disabled"

    override fun checkCondition(): Boolean? {
        return try {
            viewInteraction.getView()?.isEnabled == enabled
        } catch (e: NoMatchingViewException) {
            null
        }
    }
}

class ViewIsShown(private val viewInteraction: ViewInteraction, private val shown: Boolean) :
    Condition() {

    override val description = "text is " + if (shown) "shown" else "not shown"

    override fun checkCondition(): Boolean? {
        return try {
            viewInteraction.getView()?.isShown == shown
        } catch (e: NoMatchingViewException) {
            null
        }
    }
}

// Based on https://stackoverflow.com/a/58452045
private fun ViewInteraction.getView(): View? {
    var viewElement: View? = null
    this.perform(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun getDescription(): String {
            return "return View object"
        }

        override fun perform(uiController: UiController, view: View) {
            viewElement = view
        }
    })
    return viewElement
}
