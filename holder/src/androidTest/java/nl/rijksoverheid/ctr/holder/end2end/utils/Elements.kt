package nl.rijksoverheid.ctr.holder.end2end.utils

import android.annotation.SuppressLint
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.adevinta.android.barista.interaction.BaristaListInteractions.scrollListToPosition
import java.lang.Thread.sleep
import junit.framework.TestCase.assertNotNull
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.device
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import timber.log.Timber

object Elements {

    fun waitForText(text: String, timeout: Long = 5): UiObject2? {
        Timber.tag("end2end").d("Waiting for text '$text'")
        val element = device.wait(Until.findObject(By.textStartsWith(text)), timeout * 1000)
        assertNotNull("'$text' could not be found", element)
        return element
    }

    fun checkForText(text: String, timeout: Long = 1): Boolean {
        val textFound = device.wait(Until.hasObject(By.textContains(text)), timeout * 1000)!!
        Timber.tag("end2end").d("Checking if text '$text' was found: $textFound")
        return textFound
    }

    fun tapButton(label: String, position: Int = 0) {
        Timber.tag("end2end").d("Tapping button with label '$label'${if (position > 0) " on position $position" else ""}")
        onView(allOf(withIndex(withText(containsStringIgnoringCase(label)), position))).perform(click())
    }

    fun labelValuePairExist(label: String, value: String) {
        Timber.tag("end2end").d("Asserting label '$label' with value '$value'")
        assertContains("$label\n$value")
    }

    // Based on: https://stackoverflow.com/a/41967652
    private fun withIndex(matcher: Matcher<View?>, index: Int): Matcher<View?> {
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
        val text = eventType.internationalName
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
        Timber.tag("end2end").d("Asserting text '$text' on a view ")
        onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(text)))).check(ViewAssertions.matches(isDisplayed()))
    }

    fun Matcher<View>.tapButton(label: String) {
        Timber.tag("end2end").d("Tapping button with label '$label' on a view")
        onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(label)))).perform(click())
    }

    fun checkTextExists(text: String): Boolean {
        Timber.tag("end2end").d("Check if text '$text' exists")
        val element = device.findObject(UiSelector().textContains(text)).waitForExists(3)
        Timber.tag("end2end").d("  Result: $element")
        return element
    }

    fun enterBsn(bsn: String) {
        Timber.tag("end2end").d("Enter bsn '$bsn'")
        val browserWindow = UiScrollable(UiSelector().scrollable(true))
        val element = browserWindow.getChild(UiSelector().className(android.widget.EditText::class.java))
        element!!.click()
        element.text = bsn
        device.pressEnter()
        rest()
    }

    fun enterTextInField(index: Int, text: String): UiObject {
        Timber.tag("end2end").d("Find EditText element with index '$index' and entering text")
        val element = device.findObject(UiSelector().className(android.widget.EditText::class.java).instance(index))
        assertNotNull("EditText element with index '$index' could not be found", element)
        element!!.click()
        element.text = text
        return element
    }

    fun tapButtonElement(label: String) {
        Timber.tag("end2end").d("Find Button element with label '$label' and clicking")
        val element = device.findObject(UiSelector().className(android.widget.Button::class.java).textContains(label))
        assertNotNull("Button element with label '$label' could not be found", element)
        element!!.click()
    }

    @Suppress("unused")
    fun Any.rest(timeout: Long = 1) {
        Timber.tag("end2end").d("  Rest Here Weary Traveler, for just ${if (timeout > 1) "$timeout seconds" else "a second"}")
        sleep(timeout * 1000)
    }

    fun scrollToTextInOverview(text: String) {
        for (i in 2..12 step 2) {
            Timber.tag("end2end").d("Scrolling to position $i on overview to search for '$text'")
            scrollListToPosition(R.id.recyclerView, i)
            val found = device.findObject(By.textContains(text)) != null
            if (found) break
        }
    }
}
