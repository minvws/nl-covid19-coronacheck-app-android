/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.interaction

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
import nl.rijksoverheid.ctr.holder.end2end.model.written
import org.hamcrest.CoreMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import timber.log.Timber

object Espresso {
    fun tapButtonPosition(label: String, position: Int) {
        Timber.tag("end2end").d("Tapping button with label '$label' on position $position")
        val viewInteraction = Espresso.onView(
            withIndex(
                ViewMatchers.withText(
                    CoreMatchers.containsStringIgnoringCase(label)
                ), position
            )
        )
        waitUntilViewIsShown(viewInteraction)
        viewInteraction.perform(ViewActions.click())
    }

    fun tapWalletEvent(event: Event) {
        Timber.tag("end2end")
            .d("Tapping wallet event of type ${event.eventType.value} with date ${event.eventDate.written()}")
        val viewInteraction = Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.withChild(
                    CoreMatchers.allOf(
                        ViewMatchers.withId(R.id.title),
                        ViewMatchers.withText(CoreMatchers.containsStringIgnoringCase(event.eventType.value))
                    )
                ),
                ViewMatchers.withChild(
                    CoreMatchers.allOf(
                        ViewMatchers.withId(R.id.subtitle),
                        ViewMatchers.withText(event.eventDate.written())
                    )
                )
            )
        )
        viewInteraction.perform(ViewActions.click())
    }

    fun firstMatch(matcher: Matcher<View?>): Matcher<View?> {
        return withIndex(matcher, 0)
    }

    // Based on: https://stackoverflow.com/a/41967652
    private fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?> {
        return object : TypeSafeMatcher<View>() {
            var currentIndex = 0
            var viewObjHash = 0

            override fun describeTo(description: Description) {
                description.appendText(String.format("with index: %d ", index))
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                if (matcher.matches(view) && currentIndex++ == index) {
                    viewObjHash = view.hashCode()
                }
                return view.hashCode() == viewObjHash
            }
        }
    }

    fun card(eventType: EventType, position: Int): Matcher<View> {
        val text = eventType.internationalName
        Timber.tag("end2end")
            .d("Getting card view with text '$text'${if (position > 0) " on position $position" else ""}")
        return CoreMatchers.allOf(
            withIndex(
                CoreMatchers.allOf(
                    ViewMatchers.withId(R.id.proof_1),
                    ViewMatchers.hasDescendant(
                        CoreMatchers.allOf(
                            ViewMatchers.withId(R.id.title),
                            ViewMatchers.withText(CoreMatchers.containsStringIgnoringCase(text))
                        )
                    )
                ), position
            )
        )
    }

    fun Matcher<View>.containsText(text: String) {
        Timber.tag("end2end").d("Contains text '$text' on a view")
        val viewInteraction = Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.isDescendantOfA(this),
                ViewMatchers.withText(CoreMatchers.containsStringIgnoringCase(text))
            )
        )
        waitUntilViewIsShown(viewInteraction)
        viewInteraction.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    fun Matcher<View>.buttonIsEnabled(@IdRes viewId: Int, enabled: Boolean = true) {
        Timber.tag("end2end")
            .d("Checking if button on a view is ${if (enabled) "enabled" else "disabled"}")
        val viewInteraction = Espresso.onView(
            CoreMatchers.allOf(
                ViewMatchers.isDescendantOfA(this),
                ViewMatchers.withId(viewId)
            )
        )
        waitUntilButtonEnabled(viewInteraction, enabled)
        viewInteraction.check(
            ViewAssertions.matches(
                if (enabled) ViewMatchers.isEnabled() else CoreMatchers.not(
                    ViewMatchers.isEnabled()
                )
            )
        )
    }
}
