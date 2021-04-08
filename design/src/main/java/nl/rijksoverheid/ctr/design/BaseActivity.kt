/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design

import android.view.View
import androidx.core.view.ViewCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment


/**
 * Base activity that adjust the navigation menu for system bar insets
 */
abstract class BaseMainFragment(contentLayoutId: Int) :
    Fragment(contentLayoutId) {

    override fun onStart() {
        super.onStart()
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController
        val drawer: DrawerLayout? = requireView().findViewById(R.id.drawer_layout)
        val navView: View? = requireView().findViewById(R.id.nav_view)

        if (drawer != null && navView != null && navController != null) {
            // Listen on the root view so that we can inset the navView even if the insets are consumed
            // within the layout (for example due to using fitSystemWindows)
            ViewCompat.setOnApplyWindowInsetsListener(drawer) { _, insets ->
                navView.setPadding(
                    0,
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) insets.systemWindowInsetTop else 0,
                    0,
                    insets.systemWindowInsetBottom
                )
                insets
            }
        }
    }
}
