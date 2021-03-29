/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.ext

import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.Toolbar

fun Toolbar.getNavigationIconView(): View? {
    //check if contentDescription previously was set
    val hadContentDescription = !TextUtils.isEmpty(navigationContentDescription)
    val contentDescription =
        if (hadContentDescription) navigationContentDescription else "navigationIcon"
    navigationContentDescription = contentDescription
    val potentialViews = arrayListOf<View>()
    //find the view based on it's content description, set programatically or with android:contentDescription
    findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
    //Clear content description if not previously present
    if (!hadContentDescription) {
        navigationContentDescription = null
    }
    //Nav icon is always instantiated at this point because calling setNavigationContentDescription ensures its existence
    return potentialViews.firstOrNull()
}