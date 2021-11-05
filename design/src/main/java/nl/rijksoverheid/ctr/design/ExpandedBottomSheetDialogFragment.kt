package nl.rijksoverheid.ctr.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.databinding.BottomSheetBinding
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.shared.ext.launchUrl

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class ExpandedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val sheetInternal: View = bottomSheetDialog.findViewById(R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(sheetInternal).state = BottomSheetBehavior.STATE_EXPANDED
        }
        super.onCreateView(inflater, container, savedInstanceState)
        return BottomSheetBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = BottomSheetBinding.bind(view)

        binding.close.setOnClickListener {
            dismiss()
        }

        ViewCompat.setAccessibilityDelegate(binding.close, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                info?.setTraversalBefore(binding.description)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })

        val expandedBottomSheetData = arguments?.get(dataKey) as? BottomSheetData ?: return
        binding.title.text = expandedBottomSheetData.title
        binding.description.apply {
            expandedBottomSheetData.descriptionData.run {
                htmlText?.let {
                    setHtmlText(it, htmlLinksEnabled) }
                htmlTextString?.let {
                    setHtmlText(it, htmlLinksEnabled) }
                customLinkIntent?.let { enableCustomLinks { context.startActivity(it) } }
            }
        }
        when (expandedBottomSheetData) {
            is BottomSheetData.TitleDescription -> {}
            is BottomSheetData.TitleDescriptionWithButton -> {
                binding.button.visibility = View.VISIBLE
                binding.button.apply {
                    text = expandedBottomSheetData.buttonData.text
                    setOnClickListener { expandedBottomSheetData.buttonData.link.launchUrl(context) }
                }
            }
            is BottomSheetData.TitleDescriptionWithFooter -> {
                binding.footer.text = expandedBottomSheetData.footerText
            }
        }
    }

    companion object {
        const val dataKey = "expandedBottomSheetData"
    }
}
