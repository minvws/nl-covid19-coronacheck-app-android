/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.items

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemYourEventBinding

class YourEventWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes) {

    val binding =
        ItemYourEventBinding.inflate(LayoutInflater.from(context), this, true)

    fun setContent(position: Int, date: String, infoClickListener: () -> Unit) {
        binding.rowTitle.text = resources.getString(R.string.retrieved_vaccination_title, position)
        binding.rowSubtitle.text =
            resources.getString(R.string.retrieved_vaccination_subtitle, date)

        binding.info.setOnClickListener {
            infoClickListener.invoke()
        }
    }
}
