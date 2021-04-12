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


/**
 * Request focus for accessibility framework
 */
fun View.setAccessibilityFocus(): View {
    this.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
    this.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    return this
}

private fun View.setAccessibilityDelegate(callback: (host: View, info: AccessibilityNodeInfoCompat) -> Unit) {
    ViewCompat.setAccessibilityDelegate(
        this,
        object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfoCompat
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                callback(host, info)
            }
        }
    )
}

fun View.accessibilityHeading(isHeading: Boolean) {
    this.setAccessibilityDelegate { _, info ->
        info.isHeading = isHeading
    }
}


fun View.setAsAccessibilityButton(isButton: Boolean) {
    this.setAccessibilityDelegate { _, info ->
        info.className = if (isButton) {
            Button::class.java.name
        } else {
            this::class.java.name
        }
    }
}
