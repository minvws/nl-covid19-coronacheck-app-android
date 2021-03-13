package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogYourNegativeTestResultExplanationBinding
import nl.rijksoverheid.ctr.shared.ext.fromHtml

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class YourNegativeTestResultExplanationDialogFragment : ExpandedBottomSheetDialogFragment() {

    private lateinit var binding: DialogYourNegativeTestResultExplanationBinding
    private val args: YourNegativeTestResultExplanationDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DialogYourNegativeTestResultExplanationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.paragraph1.text =
            getString(R.string.your_negative_test_results_explanation_paragraph_1).fromHtml()
        binding.paragraph2.text =
            getString(R.string.your_negative_test_results_explanation_paragraph_2).fromHtml()

        val holder = args.holder
        binding.personalDetailsHolder.setPersonalDetails(
            items = listOf(
                holder.firstNameInitial,
                holder.lastNameInitial,
                holder.birthDay,
                holder.birthMonth
            ),
            showPosition = true
        )
    }
}

