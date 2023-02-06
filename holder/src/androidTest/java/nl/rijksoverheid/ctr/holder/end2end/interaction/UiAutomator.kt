/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.interaction

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import timber.log.Timber

fun UiDevice.checkForText(text: String, timeout: Long = 1): Boolean {
    Timber.tag("end2end").d("Checking if text '$text' was found")
    val element = this.wait(Until.hasObject(By.textContains(text)), timeout * 1_000)!!
    TestCase.assertNotNull("'$text' could not be found", element)
    return element
}

fun UiDevice.enterBsn(bsn: String) {
    Timber.tag("end2end").d("Enter bsn '$bsn'")
    val browserWindow = UiScrollable(UiSelector().scrollable(true))
    val element = browserWindow.getChild(UiSelector().className(android.widget.EditText::class.java))
    element!!.click()
    element.text = bsn
    this.pressEnter()
    Thread.sleep(2_000)
}

fun UiDevice.enterTextInField(index: Int, text: String): UiObject {
    Timber.tag("end2end").d("Find EditText element with index '$index' and entering text")
    val element = this.findObject(UiSelector().className(android.widget.EditText::class.java).instance(index))
    TestCase.assertNotNull("EditText element with index '$index' could not be found", element)
    element!!.click()
    element.text = text
    return element
}

fun UiDevice.tapButtonElement(label: String) {
    Timber.tag("end2end").d("Find Button element with label '$label' and clicking")
    this.findObject(UiSelector().className(android.widget.Button::class.java).textStartsWith(label)).click()
}

fun UiDevice.tapOnElementWithContentDescription(contentDescription: String, timeout: Long = 3) {
    Timber.tag("end2end").d("Find element with content description '$contentDescription' and clicking")
    this.wait(Until.findObject(By.desc(contentDescription)), timeout * 1_000).click()
}
