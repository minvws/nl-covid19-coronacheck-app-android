package nl.rijksoverheid.ctr.shared.ext

import android.R
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
 * - Sets max lines to 5 for a snackbar
 * - Applys activity window insets
 * @param fragmentActivity The parent activity
 */
fun Snackbar.show(fragmentActivity: FragmentActivity) {
    // Set multi lines
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5

    // Apply window insets on snackbar
    ViewCompat.setOnApplyWindowInsetsListener(
        fragmentActivity.findViewById(R.id.content)
    ) { _, insets ->
        this.view.translationY = insets.systemWindowInsetBottom * -1f
        insets
    }
    this.show()
}
