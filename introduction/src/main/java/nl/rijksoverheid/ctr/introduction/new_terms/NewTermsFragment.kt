package nl.rijksoverheid.ctr.introduction.new_terms

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.ext.enableWebLinks
import nl.rijksoverheid.ctr.design.spans.LinkTransformationMethod
import nl.rijksoverheid.ctr.design.utils.getSpannableFromHtml
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentNewTermsBinding
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewTermsFragment : Fragment(R.layout.fragment_new_terms) {

    private val args: NewTermsFragmentArgs by navArgs()
    private val introductionViewModel: IntroductionViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNewTermsBinding.bind(view)

        binding.scroll.doOnPreDraw {
            if (binding.scroll.canScrollVertically(1)) {
                binding.bottom.cardElevation =
                    resources.getDimensionPixelSize(R.dimen.onboarding_bottom_scroll_elevation)
                        .toFloat()
            } else {
                binding.bottom.cardElevation = 0f
            }
        }

        binding.highlights.text =
            getSpannableFromHtml(requireContext(), getString(R.string.new_terms_highlights))

        binding.terms.text = getString(R.string.new_terms).fromHtml()
        binding.terms.enableWebLinks()

        if (args.newTerms.needsConsent) {
            binding.positiveButton.visibility = View.VISIBLE
            binding.positiveButton.text =
                getString(R.string.new_terms_consent_needed_positive_button)
            binding.negativeButton.visibility = View.VISIBLE
            binding.negativeButton.text =
                getString(R.string.new_terms_consent_needed_negative_button)
            binding.negativeButton.setOnClickListener {
                presentNeedToConsentDialog()
            }
        } else {
            binding.positiveButton.visibility = View.VISIBLE
            binding.positiveButton.text = getString(R.string.new_terms_consent_button)
            binding.negativeButton.visibility = View.GONE
        }

        binding.positiveButton.setOnClickListener {
            introductionViewModel.saveIntroductionFinished(
                newTerms = args.newTerms
            )
            requireActivity().findNavController(R.id.main_nav_host_fragment)
                .navigate(R.id.action_main)
        }
    }

    private fun presentNeedToConsentDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.new_terms_no_consent_dialog_title)
            .setMessage(R.string.new_terms_no_consent_dialog_message)
            .setPositiveButton(
                R.string.new_terms_no_consent_dialog_button
            ) { _, _ -> }
            .show()
    }
}
