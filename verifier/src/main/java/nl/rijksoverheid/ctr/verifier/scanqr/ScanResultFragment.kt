package nl.rijksoverheid.ctr.verifier.scanqr

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultBinding

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

        if (args.valid) {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
            binding.image.setImageResource(R.drawable.illustration_scan_result_valid)
            binding.title.text = getString(R.string.scan_result_valid_title)
            binding.subtitle.text = getString(R.string.scan_result_valid_subtitle)
            binding.link.visibility = View.GONE
        } else {
            binding.root.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.red))
            binding.image.setImageResource(R.drawable.illustration_scan_result_invalid)
            binding.title.text = getString(R.string.scan_result_invalid_title)
            binding.subtitle.text = getString(R.string.scan_result_invalid_subtitle).fromHtml()

            val spannableString =
                SpannableString(getString(R.string.scan_result_invalid_link))

            spannableString.setSpan(
                UnderlineSpan(),
                0,
                getString(R.string.scan_result_invalid_link).length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.link.text = spannableString

        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.button.setOnClickListener {
            findNavController().navigate(ScanResultFragmentDirections.actionScanQr(true))
        }
    }
}
