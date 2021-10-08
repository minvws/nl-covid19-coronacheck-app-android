/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.ext

import android.view.View
import androidx.annotation.DimenRes

fun View.setVisible(isVisible: Boolean) {
    visibility =  if (isVisible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun View.getDimensionPixelSize(@DimenRes dimenRes: Int) =
    context.resources.getDimensionPixelSize(dimenRes)
