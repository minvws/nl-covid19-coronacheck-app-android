package nl.rijksoverheid.ctr.holder.myoverview.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import nl.rijksoverheid.ctr.holder.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeAnimationWidget(context: Context, attrs: AttributeSet?) :
    ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.widget_qr_code_animation, this)
    }

}
