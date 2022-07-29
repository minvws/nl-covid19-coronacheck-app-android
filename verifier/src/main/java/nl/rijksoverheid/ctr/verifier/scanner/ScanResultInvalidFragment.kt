package nl.rijksoverheid.ctr.verifier.scanner

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import java.util.concurrent.TimeUnit
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.fragment.AutoCloseFragment
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultInvalidBinding
import nl.rijksoverheid.ctr.verifier.policy.VerificationPolicySelectionState
import nl.rijksoverheid.ctr.verifier.policy.VerificationPolicySelectionStateUseCase
import nl.rijksoverheid.ctr.verifier.scanner.models.ScanResultInvalidData
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanResultInvalidFragment : AutoCloseFragment(R.layout.fragment_scan_result_invalid) {

    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val selectionStateUseCase: VerificationPolicySelectionStateUseCase by inject()

    private val args: ScanResultInvalidFragmentArgs by navArgs()

    override fun aliveForMilliseconds(): Long {
        return if (BuildConfig.FLAVOR == "acc") {
            TimeUnit.SECONDS.toMillis(20)
        } else {
            TimeUnit.MINUTES.toMillis(3)
        }
    }

    override fun navigateToCloseAt() {
        navigateSafety(
            R.id.nav_scan_result_invalid,
            ScanResultInvalidFragmentDirections.actionNavQrScanner()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanResultInvalidBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener { navigateToScanner() }

        binding.bottom.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
        binding.bottom.customiseButton {
            it.run {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.surface)
            }
        }
        binding.bottom.customiseSecondaryButton {
            it.run {
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.black)
            }
        }

        when (args.invalidData) {
            is ScanResultInvalidData.Invalid -> {
                binding.title.text = getString(R.string.scan_result_european_nl_invalid_title)
                binding.bottom.customiseSecondaryButton {
                    it.visibility = View.INVISIBLE
                }
            }
            is ScanResultInvalidData.Error -> {
                binding.bottom.setSecondaryButtonClick {
                    infoFragmentUtil.presentAsBottomSheet(
                        childFragmentManager,
                        InfoFragmentData.TitleDescription(
                            title = getString(R.string.scan_result_invalid_reason_title),
                            descriptionData = DescriptionData(
                                when (selectionStateUseCase.get()) {
                                    is VerificationPolicySelectionState.Selection,
                                    is VerificationPolicySelectionState.Policy1G -> R.string.scan_result_invalid_reason_description_1G
                                    is VerificationPolicySelectionState.Policy3G -> R.string.scan_result_invalid_reason_description
                                }
                            )
                        )
                    )
                }
            }
        }

        args.title?.let {
            binding.title.text = it
        }

        binding.bottom.setButtonClick { navigateToScanner() }

        // scroll all the way down on landscape
        // so the user notices what can be done
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.scroll.post {
                binding.scroll.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private fun navigateToScanner() {
        navigateSafety(
            R.id.nav_scan_result_invalid,
            ScanResultInvalidFragmentDirections.actionNavQrScanner()
        )
    }
}
