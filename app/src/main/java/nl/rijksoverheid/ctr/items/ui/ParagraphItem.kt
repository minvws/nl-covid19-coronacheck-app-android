/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.items.ui

import android.text.method.LinkMovementMethod
import androidx.core.view.ViewCompat
import com.xwray.groupie.Item
import nl.rijksoverheid.ctr.R
import nl.rijksoverheid.ctr.databinding.ItemParagraphBinding
import nl.rijksoverheid.ctr.items.BaseBindableItem
import nl.rijksoverheid.ctr.util.HtmlHelper
import timber.log.Timber

class ParagraphItem(
    private val text: String?,
    private val clickable: Boolean = false
) : BaseBindableItem<ItemParagraphBinding>() {
    override fun getLayout() = R.layout.item_paragraph

    override fun bind(viewBinding: ItemParagraphBinding, position: Int) {
        ViewCompat.enableAccessibleClickableSpanSupport(viewBinding.content)
        viewBinding.content.linksClickable = true
        viewBinding.content.movementMethod = LinkMovementMethod.getInstance();

        Timber.d("Got value $text")
        text?.let {
            val context = viewBinding.root.context
            val spannableBuilder = HtmlHelper.buildSpannableFromHtml(it, context)
            viewBinding.text = spannableBuilder
        }
    }

    override fun isClickable() = clickable
    override fun isSameAs(other: Item<*>): Boolean = other is ParagraphItem && other.text == text
    override fun hasSameContentAs(other: Item<*>) = other is ParagraphItem && other.text == text
}