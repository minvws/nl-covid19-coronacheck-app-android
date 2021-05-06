package nl.rijksoverheid.ctr.shared.utils

import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import androidx.core.view.accessibility.AccessibilityEventCompat

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
object Accessibility {

    /**
     * Returns the AccessibilityManager if available and enabled.
     *
     * @param context Context reference
     *
     * @return AccessibilityManager object, or null
     */
    fun accessibilityManager(context: Context?): AccessibilityManager? {
        if (context != null) {
            val service = ContextCompat.getSystemService(context, AccessibilityManager::class.java)
            if (service is AccessibilityManager && service.isEnabled) {
                return service
            }
        }
        return null
    }

    /**
     * Interrupts the assistive technology
     *
     * @param context Context reference
     */
    fun interrupt(context: Context?) {
        accessibilityManager(context)?.interrupt()
    }

    /**
     * Announces the given message using the assistive technology
     *
     * @param context Context reference
     * @param message The message to announce
     */
    fun announce(context: Context?, message: String) {
        accessibilityManager(context)?.let { accessibilityManager ->
            val event = AccessibilityEvent.obtain(AccessibilityEventCompat.TYPE_ANNOUNCEMENT)
            event.text.add(message)

            accessibilityManager.sendAccessibilityEvent(event)
        }
    }

    /**
     * Moves the accessibility focus to the given view
     *
     * @param view View to move accessibility focus to
     */
    fun focus(view: View) {
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }
}

