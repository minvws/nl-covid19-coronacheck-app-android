package nl.rijksoverheid.ctr.shared.ext

import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.material.snackbar.Snackbar


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2 
 *
 */

/**
 * Sets max lines to 5 for a Snackbar
 * Apply hack to get snackbar above navbar for api < 29 with windowTranslucentNavigation set to true
 */
fun Snackbar.show(fragmentActivity: FragmentActivity) {
    // Set multi lines
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5

    // Apply window insets on snackbar so it appears above navbar
    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
        ViewCompat.setOnApplyWindowInsetsListener(
            fragmentActivity.findViewById(android.R.id.content)
        ) { _, insets ->
            this.view.translationY = insets.systemWindowInsetBottom * -1f
            insets
        }
    }

    this.show()
}
