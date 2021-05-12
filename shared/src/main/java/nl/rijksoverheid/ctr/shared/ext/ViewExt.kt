/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.ext

import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import nl.rijksoverheid.ctr.shared.utils.Accessibility


/**
 * Request focus for accessibility framework
 */
//fun View.setAccessibilityFocus(): View {
//    Accessibility.focus(this)
//    return this
//}
//
//fun View.setAsAccessibilityHeading(isHeading: Boolean = true) {
//    Accessibility.heading(this, isHeading)
//}
//
//
//fun View.setAsAccessibilityButton(isButton: Boolean) {
//    Accessibility.button(this, isButton)
//}