package nl.rijksoverheid.ctr.shared.ext

import android.widget.TextView
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
 */
fun Snackbar.show() {
    // Set multi lines
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).maxLines = 5
    this.show()
}
