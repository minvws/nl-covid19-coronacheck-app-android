package nl.rijksoverheid.ctr.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun handleAccessibility(
        container: ConstraintLayout,
        titleView: View? = null,
        @StringRes description: Int
    ) {
        val set = ConstraintSet()

        val closeButton = ImageView(requireContext())
        closeButton.id = View.generateViewId()
        closeButton.setImageResource(nl.rijksoverheid.ctr.design.R.drawable.ic_close)
        closeButton.contentDescription = getString(description)
        container.addView(closeButton, 0)

        set.clone(container)
        // Constraint the button top to the top and end of the container
        set.connect(closeButton.id, ConstraintSet.TOP, container.id, ConstraintSet.TOP)
        set.connect(closeButton.id, ConstraintSet.END, container.id, ConstraintSet.END)

        // If a title view is given, constraint this to the new button to avoid overlap
        if (titleView != null) {
            set.connect(titleView.id, ConstraintSet.END, closeButton.id, ConstraintSet.START)
        }
        set.applyTo(container)

        closeButton.setOnClickListener {
            dismiss()
        }

        // Sets the focus to the close button on initializing as requested in WCAG feedback
        ViewCompat.setAccessibilityDelegate(closeButton, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfoCompat?) {
                info?.setTraversalBefore(titleView)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })
    }
}
