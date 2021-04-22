package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.ext.isScreenReaderOn
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogYourNegativeTestResultExplanationBinding
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogYourNegativeTestResultExplanationBinding.bind(view)
        binding.paragraph1.text =
            getString(R.string.your_negative_test_results_explanation_paragraph_1).fromHtml()
        binding.paragraph2.text =
            getString(R.string.your_negative_test_results_explanation_paragraph_2).fromHtml()

        val holder = args.holder

        val personalDetails = personalDetailsUtil.getPersonalDetails(
            firstNameInitial = holder.firstNameInitial,
            lastNameInitial = holder.lastNameInitial,
            birthDay = holder.birthDay,
            birthMonth = holder.birthMonth,
            includeBirthMonthNumber = false
        )

        binding.firstNameInitial.itemText.text = personalDetails.firstNameInitial
        binding.firstNameInitial.itemPosition.text = "1"

        binding.lastNameInitial.itemText.text = personalDetails.lastNameInitial
        binding.lastNameInitial.itemPosition.text = "2"

        binding.birthDay.itemText.text = personalDetails.birthDay
        binding.birthDay.itemPosition.text = "3"

        binding.birthMonth.itemText.text = personalDetails.birthMonth
        binding.birthMonth.itemPosition.text = "4"

        if (requireContext().isScreenReaderOn()) {
            handleAccessibility(binding.container, binding.title, R.string.menu_close)
        }
    }
}

