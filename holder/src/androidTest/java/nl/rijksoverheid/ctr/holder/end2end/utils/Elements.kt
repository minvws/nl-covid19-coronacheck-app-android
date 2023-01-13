package nl.rijksoverheid.ctr.holder.end2end.utils

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions
import com.adevinta.android.barista.interaction.BaristaClickInteractions
import com.adevinta.android.barista.interaction.BaristaListInteractions
import com.adevinta.android.barista.interaction.BaristaScrollInteractions
import java.lang.Thread.sleep
import junit.framework.TestCase.assertNotNull
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.BaseTest.Companion.device
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.wait.ButtonInState
import nl.rijksoverheid.ctr.holder.end2end.wait.ViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.wait.Wait
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import timber.log.Timber

object Elements {

    // MARK: Barista

    fun assertContains(text: String) {
        Timber.tag("end2end").d("Asserting contains text '$text'")
        BaristaVisibilityAssertions.assertContains(text)
    }

    fun assertDisplayed(text: String) {
        Timber.tag("end2end").d("Asserting displayed text '$text'")
        BaristaVisibilityAssertions.assertDisplayed(text)
    }

    fun assertNotExist(text: String) {
        Timber.tag("end2end").d("Asserting not displayed text '$text'")
        BaristaVisibilityAssertions.assertNotExist(text)
    }

    fun assertNotDisplayed(text: String) {
        Timber.tag("end2end").d("Asserting not displayed text '$text'")
        BaristaVisibilityAssertions.assertNotDisplayed(text)
    }

    fun assertNotDisplayed(@IdRes viewId: Int) {
        Timber.tag("end2end").d("Asserting not displayed view with ID '$viewId'")
        BaristaVisibilityAssertions.assertNotDisplayed(viewId)
    }

    fun clickOn(text: String) {
        Timber.tag("end2end").d("Clicking on '$text'")
        Wait.until(ButtonInState(onView(withText(text)), true))
        BaristaClickInteractions.clickOn(text)
    }

    fun clickOn(@IdRes resId: Int) {
        Timber.tag("end2end").d("Clicking on resource with id '$resId'")
        Wait.until(ViewIsShown(onView(withId(resId)), true))
        BaristaClickInteractions.clickOn(resId)
    }

    fun clickBack() {
        Timber.tag("end2end").d("Clicking back")
        BaristaClickInteractions.clickBack()
    }

    fun scrollTo(text: String) {
        Timber.tag("end2end").d("Scrolling to view with text '$text'")
        BaristaScrollInteractions.scrollTo(text)
    }

    fun labelValuePairExist(label: String, value: String) {
        Timber.tag("end2end").d("Asserting label '$label' with value '$value'")
        BaristaVisibilityAssertions.assertContains("$label\n$value")
    }

    fun scrollListToPosition(@IdRes resId: Int, position: Int) {
        Timber.tag("end2end").d("Scrolling to position '$position' on view with ID '$resId'")
        BaristaListInteractions.scrollListToPosition(resId, position)
    }

    // MARK: Espresso

    fun tapButton(label: String, position: Int = 0) {
        Timber.tag("end2end").d("Tapping button with label '$label'${if (position > 0) " on position $position" else ""}")
        val viewInteraction = onView(allOf(withIndex(withText(containsStringIgnoringCase(label)), position)))
        Wait.until(ViewIsShown(viewInteraction, true))
        viewInteraction.perform(click())
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

    // Based on https://stackoverflow.com/a/58452045
    fun ViewInteraction.getView(): View? {
        var viewElement: View? = null
        this.perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isAssignableFrom(View::class.java)
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

    fun card(eventType: Event.Type): Matcher<View> {
        val text = eventType.internationalName
        Timber.tag("end2end").d("Getting card view with text '$text'")
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
        Timber.tag("end2end").d("Contains text '$text' on a view")
        val viewInteraction = onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(text))))
        Wait.until(ViewIsShown(viewInteraction, true))
        viewInteraction.check(ViewAssertions.matches(isDisplayed()))
    }

    fun Matcher<View>.buttonIsEnabled(enabled: Boolean = true) {
        Timber.tag("end2end").d("Checking if button on a view is ${if (enabled) "enabled" else "disabled"}")
        val viewInteraction = onView(allOf(isDescendantOfA(this), withId(R.id.button)))
        Wait.until(ButtonInState(viewInteraction, enabled))
        viewInteraction.check(ViewAssertions.matches(if (enabled) isEnabled() else not(isEnabled())))
    }

    fun Matcher<View>.tapButton(label: String) {
        Timber.tag("end2end").d("Tapping button with label '$label' on a view")
        val viewInteraction = onView(allOf(isDescendantOfA(this), withText(containsStringIgnoringCase(label))))
        Wait.until(ButtonInState(viewInteraction, true))
        viewInteraction.perform(click())
    }

    // MARK: UiAutomator

    fun waitForText(text: String, timeout: Long = 5): UiObject2? {
        Timber.tag("end2end").d("Waiting for text '$text'")
        val element = device.wait(Until.findObject(By.textStartsWith(text)), timeout * 1000)
        assertNotNull("'$text' could not be found", element)
        return element
    }

    fun waitForView(resource: String, timeout: Long = 5) {
        Timber.tag("end2end").d("Waiting for view with ID '$resource'")
        val element = device.wait(Until.findObject(By.res(device.currentPackageName, resource)), timeout * 1000)
        assertNotNull("'$resource' could not be found", element)
    }

    fun checkForText(text: String, timeout: Long = 1): Boolean {
        Timber.tag("end2end").d("Checking if text '$text' was found")
        val element = device.wait(Until.hasObject(By.textContains(text)), timeout * 1000)!!
        assertNotNull("'$text' could not be found", element)
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
        device.findObject(UiSelector().className(android.widget.Button::class.java).textStartsWith(label)).click()
    }

    // MARK: Other

    @Suppress("unused")
    fun Any.rest(timeout: Long = 1) {
        Timber.tag("end2end").d("  Rest Here Weary Traveler, for just ${if (timeout > 1) "$timeout seconds" else "a second"}")
        sleep(timeout * 1000)
    }
}
