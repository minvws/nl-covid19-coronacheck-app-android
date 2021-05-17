package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogYourNegativeTestResultExplanationBinding
import nl.rijksoverheid.ctr.holder.databinding.ViewDialogYourNegativeTestResultExplanationPersonalDetailBinding
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class YourNegativeTestResultExplanationDialogFragment : ExpandedBottomSheetDialogFragment() {

    private val args: YourNegativeTestResultExplanationDialogFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return DialogYourNegativeTestResultExplanationBinding.inflate(inflater).root
    }

    private fun bindDetails(binding: ViewDialogYourNegativeTestResultExplanationPersonalDetailBinding, position: String, details: String) {
        binding.itemPosition.text = position
        binding.itemText.text = details
        binding.itemLayout.contentDescription = getString(R.string.your_negative_test_results_characteristic_format, position, details)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogYourNegativeTestResultExplanationBinding.bind(view)

        val holder = args.holder

        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = holder.firstNameInitial,
            lastNameInitial = holder.lastNameInitial,
            birthDay = holder.birthDay,
            birthMonth = holder.birthMonth,
            includeBirthMonthNumber = false
        )

        bindDetails(binding.firstNameInitial, "1", personalDetails.firstNameInitial)
        bindDetails(binding.lastNameInitial, "2", personalDetails.lastNameInitial)
        bindDetails(binding.birthDay, "3", personalDetails.birthDay)
        bindDetails(binding.birthMonth, "4", personalDetails.birthMonth)

        binding.close.setOnClickListener {
            dismiss()
        }

        ViewCompat.setAccessibilityDelegate(binding.close, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                info?.setTraversalBefore(binding.title)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })
    }
}

