/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment

/**
 * Base activity that adjust the navigation menu for system bar insets and
 * locks the drawer on any nav destination except for the home destination
 * @param homeDestination the destination which should show the drawer
 */
abstract class BaseActivity(@IdRes private val homeDestination: Int) : AppCompatActivity() {
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController
        val drawer: DrawerLayout? = findViewById(R.id.drawer_layout)
        val navView: View? = findViewById(R.id.nav_view)

        if (drawer != null && navView != null && navController != null) {
            // Listen on the root view so that we can inset the navView even if the insets are consumed
            // within the layout (for example due to using fitSystemWindows)
            ViewCompat.setOnApplyWindowInsetsListener(drawer) { _, insets ->
                navView.setPadding(0, insets.systemWindowInsetTop, 0, 0)
                insets
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    homeDestination -> drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    else -> drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }
    }
}
