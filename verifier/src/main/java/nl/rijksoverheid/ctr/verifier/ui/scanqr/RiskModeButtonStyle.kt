package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButton
import nl.rijksoverheid.ctr.verifier.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
object RiskModeButtonLayout {
    fun style(button: MaterialButton) = button.run {
        text = "Aanduiding"
        val red = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error))
        setBackgroundColor(ContextCompat.getColor(context, R.color.light_blue))
        strokeWidth = 0
        setIconResource(R.drawable.ic_shield)
        iconTint = red
        iconSize = resources.getDimensionPixelSize(R.dimen.home_risk_mode_button_icon_size)
        setSingleLine()
        setTextColor(red)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.home_risk_mode_button_text_size))
        visibility = View.VISIBLE
        val paddingPixels = resources.getDimensionPixelSize(R.dimen.home_risk_mode_button_horizontal_padding)
        setPadding(paddingPixels, 0, paddingPixels, 0)
        updateLayoutParams<ConstraintLayout.LayoutParams> {
            height = resources.getDimensionPixelSize(R.dimen.home_risk_mode_button_height)
            marginStart = resources.getDimensionPixelSize(R.dimen.home_risk_mode_button_horizontal_margin)
            marginEnd = resources.getDimensionPixelSize(R.dimen.home_risk_mode_button_horizontal_margin)
        }
    }
}