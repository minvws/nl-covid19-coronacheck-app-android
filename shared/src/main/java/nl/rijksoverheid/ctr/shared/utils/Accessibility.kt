package nl.rijksoverheid.ctr.shared.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat

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
     * Checks whether any kind of screen reader is active
     *
     * @param context Context reference
     */
    fun screenReader(context: Context?): Boolean {
        return accessibilityManager(context)?.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_SPOKEN
        )?.isNotEmpty() ?: false
    }

    /**
     * Checks whether touch exploration is active
     *
     * @param context Context reference
     */
    fun touchExploration(context: Context?): Boolean {
        return accessibilityManager(context)?.isTouchExplorationEnabled ?: false
    }

    /**
     * Moves the accessibility focus to the given view
     *
     * @param view View to move accessibility focus to
     */
    fun focus(view: View): View {
        view.isFocusable = true
        view.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        return view
    }

    /**
     * Extension to move the accessibility focus to the given view
     */
    fun View.setAccessibilityFocus(): View {
        return focus(this)
    }

    /**
     * Helper method to set accessibility delegate with callback
     *
     * @param view View to set the delegate of
     * @param callback Callback used to set properties of AccessibilityNodeInfoCompat
     */
    fun accessibilityDelegate(view: View, callback: (host: View, info: AccessibilityNodeInfoCompat) -> Unit) {
        ViewCompat.setAccessibilityDelegate(
                view,
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

    /**
     * Helper method to mark a view as accessibility heading
     *
     * @param view View to mark
     * @param isHeading Value to apply
     */
    fun heading(view: View, isHeading: Boolean = true): View {
        accessibilityDelegate(view) { _, info ->
            info.isHeading = isHeading
        }
        return view
    }

    /**
     * Extension to mark the given view as accessibility heading
     *
     * @param isHeading Value to apply
     */
    fun View.setAsAccessibilityHeading(isHeading: Boolean = true): View {
        return heading(this, isHeading)
    }

    /**
     * Helper method to mark a view as accessibility button
     *
     * @param view View to mark
     * @param isButton Value to apply
     */
    fun button(view: View, isButton: Boolean = true): View {
        accessibilityDelegate(view) { _, info ->
            info.className = if (isButton) {
                Button::class.java.name
            } else {
                this::class.java.name
            }
        }
        return view
    }

    /**
     * Extension to mark the given view as accessibility button
     *
     * @param isButton Value to apply
     */
    fun View.setAsAccessibilityButton(isButton: Boolean = true): View {
        return button(this, isButton)
    }

    /**
     * Adds an AccessibilityAction of the given type to the given view
     *
     * @param view The view to set the action to
     * @param type Type of the action, listed in AccessibilityNodeInfoCompat
     * @param description Short description of the action
     *
     * @see androidx.core.view.accessibility.AccessibilityNodeInfoCompat
     */
    fun action(view: View, type: Int, description: CharSequence): View {
        accessibilityDelegate(view) { _, info ->
            val action = AccessibilityNodeInfoCompat.AccessibilityActionCompat(type, description)
            info.addAction(action)
        }
        return view
    }

    /**
     * Extension to add an accessibility action to the the given view
     *
     * @param type Type of the action, listed in AccessibilityNodeInfoCompat
     * @param description Short description of the action
     */
    fun View.addAccessibilityAction(type: Int, description: CharSequence): View {
        return action(this, type, description)
    }

    /**
     * Helper method to mark a view as accessibility heading
     *
     * @param view View to label
     * @param label The label to set
     */
    fun label(view: View, label: CharSequence): View {
        view.contentDescription = label
        return view
    }

    /**
     * Extension to add an accessibility label to the the given view
     *
     * @param label The label to set
     */
    fun View.setAccessibilityLabel(label: CharSequence): View {
        return label(this, label)
    }
}