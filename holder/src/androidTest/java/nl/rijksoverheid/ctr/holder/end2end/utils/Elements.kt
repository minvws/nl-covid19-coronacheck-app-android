package nl.rijksoverheid.ctr.holder.end2end.utils

import android.annotation.SuppressLint
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertContains
import junit.framework.Assert.assertNotNull
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.device
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object Elements {

    fun waitForText(text: String, timeout: Long = 5): UiObject2? {
        val element = device.wait(Until.findObject(By.textStartsWith(text)), timeout * 1000)
        assertNotNull(element)
        return element
    }

    fun checkForText(text: String, timeout: Long = 15): Boolean {
        return device.wait(Until.hasObject(By.textStartsWith(text)), timeout * 1000)!!
    }

    fun tapButton(label: String, position: Int = 0) {
        onView(allOf(withIndex(withText(containsStringIgnoringCase(label)), position))).perform(click())
    }

    fun labelValuePairExist(label: String, value: String) {
        assertContains("$label\n$value")
    }

    // Based on: https://stackoverflow.com/a/41967652
    private fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?>? {
        return object : TypeSafeMatcher<View>() {
            var currentIndex = 0
            var viewObjHash = 0

            @SuppressLint("DefaultLocale")
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

    fun card(eventType: Event.Type): Matcher<View> {
        val text = eventType.domesticName
        return allOf(
            withId(R.id.proof_1),
            hasDescendant(
                allOf(
                    withId(R.id.title),
                    withText(containsStringIgnoringCase(text))
                )
            )
        )
    }

    fun Matcher<View>.containsText(text: String) {
        onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(text))))
    }

    fun Matcher<View>.tapButton(text: String) {
        onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(text)))).perform(click())
    }
}
