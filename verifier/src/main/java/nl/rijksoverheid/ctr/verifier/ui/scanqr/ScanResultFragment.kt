package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultBinding
import org.koin.android.ext.android.inject


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanResultFragment : DialogFragment() {

    private lateinit var binding: FragmentScanResultBinding
    private val args: ScanResultFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.AppTheme_Dialog_FullScreen
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScanResultBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.validatedResult.valid) {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            binding.image.setImageResource(R.drawable.illustration_scan_result_valid)
            binding.title.text = getString(R.string.scan_result_valid_title)
            binding.subtitle.text = getString(R.string.scan_result_valid_subtitle).fromHtml()

            args.validatedResult.qrResult?.let {
                val personalDetails = personalDetailsUtil.getPersonalDetails(
                    it.firstNameInitial,
                    it.lastNameInitial,
                    it.birthDay,
                    it.birthMonth
                )
                binding.personalDetailsHolder.setPersonalDetails(personalDetails)
                binding.personalDetailsHolder.visibility = View.VISIBLE
            }

        } else {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.image.setImageResource(R.drawable.illustration_scan_result_invalid)
            binding.title.text = getString(R.string.scan_result_invalid_title)
            binding.subtitle.text = getString(R.string.scan_result_invalid_subtitle).fromHtml()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.button.setOnClickListener {
            setFragmentResult(
                ScanQrFragment.REQUEST_KEY,
                bundleOf(ScanQrFragment.EXTRA_LAUNCH_SCANNER to true)
            )
            findNavController().popBackStack()
        }
    }
}
