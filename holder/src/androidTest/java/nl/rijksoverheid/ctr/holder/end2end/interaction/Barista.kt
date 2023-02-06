/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.interaction

import androidx.annotation.IdRes
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions
import com.adevinta.android.barista.interaction.BaristaClickInteractions
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions
import com.adevinta.android.barista.interaction.BaristaListInteractions
import com.adevinta.android.barista.interaction.BaristaScrollInteractions
import timber.log.Timber

fun assertContains(text: String) {
    Timber.tag("end2end").d("Asserting contains text '$text'")
    BaristaVisibilityAssertions.assertContains(text)
}

fun assertDisplayed(text: String) {
    Timber.tag("end2end").d("Asserting displayed text '$text'")
    BaristaVisibilityAssertions.assertDisplayed(text)
}

fun assertDisplayed(@IdRes viewId: Int) {
    Timber.tag("end2end").d("Asserting displayed view '$viewId'")
    BaristaVisibilityAssertions.assertDisplayed(viewId)
}

fun assertNotExist(text: String) {
    Timber.tag("end2end").d("Asserting not existing text '$text'")
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

fun assertNotContains(text: String) {
    Timber.tag("end2end").d("Asserting not contains text '$text'")
    BaristaVisibilityAssertions.assertNotContains(text)
}

fun clickOn(text: String) {
    Timber.tag("end2end").d("Clicking on '$text'")
    BaristaClickInteractions.clickOn(text)
}

fun clickOn(@IdRes resId: Int) {
    Timber.tag("end2end").d("Clicking on resource with id '$resId'")
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

fun writeTo(@IdRes editTextId: Int, text: String) {
    Timber.tag("end2end").d("Writing text '$text' on edit text ID '$editTextId'")
    BaristaEditTextInteractions.writeTo(editTextId, text)
}
