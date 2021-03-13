/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidReasonBinding
import org.koin.android.ext.android.inject

class ScanResultValidExplanationDialogFragment : ExpandedBottomSheetDialogFragment() {

    private lateinit var binding: FragmentScanResultValidReasonBinding
    private val args: ScanResultValidExplanationDialogFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentScanResultValidReasonBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val qrResult = args.qrResult
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            qrResult.firstNameInitial,
            qrResult.lastNameInitial,
            qrResult.birthDay,
            qrResult.birthMonth
        )
        binding.personalDetailsHolder.setPersonalDetails(personalDetails, true)
    }
}
