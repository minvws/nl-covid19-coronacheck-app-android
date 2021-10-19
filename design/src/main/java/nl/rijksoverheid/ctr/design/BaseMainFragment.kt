/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design

import android.content.res.Configuration
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import nl.rijksoverheid.ctr.design.databinding.MenuHeaderBinding
import nl.rijksoverheid.ctr.shared.AccessibilityConstants
import nl.rijksoverheid.ctr.shared.ext.children
import nl.rijksoverheid.ctr.shared.utils.Accessibility

/**
 * Base activity that adjust the navigation menu for system bar insets and handles custom destination logic
 */
abstract class BaseMainFragment(
    contentLayoutId: Int,
    val topLevelDestinations: Set<Int>
) :
    Fragment(contentLayoutId) {

    override fun onStart() {
        super.onStart()
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController
        val drawer: DrawerLayout? = requireView().findViewById(R.id.drawer_layout)
        val navView: NavigationView? = requireView().findViewById(R.id.nav_view)

        if (drawer != null && navView != null && navController != null) {
            // Listen on the root view so that we can inset the navView even if the insets are consumed
            // within the layout (for example due to using fitSystemWindows)
            ViewCompat.setOnApplyWindowInsetsListener(drawer) { _, insets ->
                navView.setPadding(
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) insets.systemWindowInsetLeft else 0,
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) insets.systemWindowInsetTop else 0,
                    0,
                    insets.systemWindowInsetBottom
                )
                insets
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (topLevelDestinations.contains(destination.id)) {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }

            // Only add header if none has been added before
            if (navView.headerCount == 0) {
                // Add close button to menu for accessibility
                val menuHeader = MenuHeaderBinding.inflate(layoutInflater)
                menuHeader.menuCloseButton.setOnClickListener {
                    drawer.closeDrawer(GravityCompat.START)
                }

                // Set insets so close button is below statusbar
                ViewCompat.setOnApplyWindowInsetsListener(menuHeader.root) { view, insets ->
                    view.setPadding(0, insets.systemWindowInsetTop, 0, 0)
                    insets
                }

                navView.addHeaderView(menuHeader.root)
            }

            // Track menu opening to move focus for accessibility
            drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerStateChanged(newState: Int) {
                    if (newState == DrawerLayout.STATE_IDLE) {
                        drawer.postDelayed({
                            Accessibility.focus(
                                if (drawer.isDrawerOpen(GravityCompat.START) && isAdded) {
                                    requireView().findViewById<ImageView>(R.id.menu_close_button)
                                } else {
                                    drawer
                                }
                            )
                        }, AccessibilityConstants.ACCESSIBILITY_FOCUS_DELAY)
                    }
                }
            })
        }

        // Improve NavigationView accessibility
        navView?.postDelayed({
            navView.children().filterIsInstance<AppCompatCheckedTextView>().forEach { view ->
                Accessibility.accessibilityDelegate(view) { _, info ->
                    info.isSelected = view.isChecked
                    info.isCheckable = false
                    info.isChecked = false
                    info.className = Button::class.java.name
                }
            }
        }, AccessibilityConstants.ACCESSIBILITY_DELEGATE_DELAY)
    }
}