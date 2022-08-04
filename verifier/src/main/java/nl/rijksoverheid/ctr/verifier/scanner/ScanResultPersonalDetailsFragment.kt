/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.verifier.scanner

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import androidx.navigation.fragment.navArgs
import java.util.Locale
import java.util.concurrent.TimeUnit
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.flagEmoji
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.fragment.AutoCloseFragment
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidPersonalDetailsBinding
import org.koin.android.ext.android.inject

class ScanResultPersonalDetailsFragment :
    AutoCloseFragment(R.layout.fragment_scan_result_valid_personal_details) {

    private var _binding: FragmentScanResultValidPersonalDetailsBinding? = null
    private val binding get() = _binding!!

    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    private val args: ScanResultPersonalDetailsFragmentArgs by navArgs()

    override fun aliveForMilliseconds(): Long {
        return if (BuildConfig.FLAVOR == "acc") {
            TimeUnit.SECONDS.toMillis(20)
        } else {
            TimeUnit.MINUTES.toMillis(3)
        }
    }

    override fun navigateToCloseAt() {
        navigateSafety(
            R.id.nav_scan_result_personal_details,
            ScanResultPersonalDetailsFragmentDirections.actionNavMain()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScanResultValidPersonalDetailsBinding.bind(view)
        bindButtons()
        presentPersonalDetails()
    }

    private fun bindButtons() {
        binding.bottom.run {
            setButtonClick {
                navigateSafety(
                    ScanResultPersonalDetailsFragmentDirections.actionNavScanResultValid(args.validData)
                )
            }
            setSecondaryButtonClick {
                navigateSafety(
                    ScanResultPersonalDetailsFragmentDirections
                        .actionNavDetailsWrong(args.validData.externalReturnAppData)
                )
            }
            if (args.validData.externalReturnAppData != null) setIcon(R.drawable.ic_deeplink)
        }
        binding.toolbar.setNavigationOnClickListener { findNavControllerSafety()?.popBackStack() }
    }

    private fun presentPersonalDetails() {
        val testResultAttributes = args.validData.verifiedQr.details
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            testResultAttributes.firstNameInitial,
            testResultAttributes.lastNameInitial,
            testResultAttributes.birthDay,
            testResultAttributes.birthMonth,
            includeBirthMonthNumber = true
        )
        binding.personalDetailsLastname.setContent(personalDetails.lastNameInitial)
        binding.personalDetailsFirstname.setContent(personalDetails.firstNameInitial)
        binding.personalDetailsBirthmonth.setContent(personalDetails.birthMonth)
        binding.personalDetailsBirthdate.setContent(personalDetails.birthDay)
        if (testResultAttributes.isInternationalDCC()) {
            binding.internationalDescription.visibility = View.VISIBLE
            val dccLocale = Locale("", testResultAttributes.issuerCountryCode)
            val text = getString(
                R.string.scan_result_valid_international_scanned,
                dccLocale.flagEmoji
            )
            if (dccLocale.flagEmoji.isNotEmpty()) {
                binding.internationalDescription.text = increasedSizeFlagEmoji(text)
            }
        }
    }

    private fun increasedSizeFlagEmoji(textWithEmoji: String): SpannableString {
        val flagSpannableString = SpannableString(textWithEmoji)
        flagSpannableString.setSpan(
            AbsoluteSizeSpan(28, true), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return flagSpannableString
    }
}
