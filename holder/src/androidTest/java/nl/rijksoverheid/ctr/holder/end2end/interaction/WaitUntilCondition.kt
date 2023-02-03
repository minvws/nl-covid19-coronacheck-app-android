/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.interaction

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import nl.rijksoverheid.ctr.holder.end2end.interaction.wait.ButtonInState
import nl.rijksoverheid.ctr.holder.end2end.interaction.wait.ViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.interaction.wait.Wait
import org.hamcrest.Matcher

fun waitUntilTextIsShown(text: String, timeout: Long = 5) {
    Wait.until(ViewIsShown(onView(withText(text)), true), timeoutLimit = timeout * 1000)
}

fun waitUntilViewIsShown(@IdRes viewId: Int, timeout: Long = 5) {
    Wait.until(ViewIsShown(onView(withId(viewId)), true), timeoutLimit = timeout * 1000)
}

fun waitUntilViewIsShown(viewInteraction: ViewInteraction, timeout: Long = 5) {
    Wait.until(ViewIsShown(viewInteraction, true), timeoutLimit = timeout * 1000)
}

fun waitUntilViewIsShown(matcher: Matcher<View>, timeout: Long = 5) {
    Wait.until(ViewIsShown(onView(matcher), true), timeoutLimit = timeout * 1000)
}

fun waitUntilButtonEnabled(viewInteraction: ViewInteraction, enabled: Boolean, timeout: Long = 5) {
    Wait.until(ButtonInState(viewInteraction, enabled), timeoutLimit = timeout * 1000)
}
