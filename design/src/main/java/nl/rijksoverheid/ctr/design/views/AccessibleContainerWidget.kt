/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.design.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import nl.rijksoverheid.ctr.design.databinding.ViewIdentifierItemBinding
import nl.rijksoverheid.ctr.design.databinding.WidgetAccessibleContainerBinding
import nl.rijksoverheid.ctr.design.ext.isScreenReaderOn

class AccessibleContainerWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    val binding = WidgetAccessibleContainerBinding.inflate(LayoutInflater.from(context), this)
    init {
        if(context.isScreenReaderOn()){
            binding.closeBtn.visibility = View.VISIBLE
        }
    }

}
