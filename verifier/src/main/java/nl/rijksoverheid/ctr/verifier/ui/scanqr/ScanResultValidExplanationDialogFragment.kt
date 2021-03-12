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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidReasonBinding
import org.koin.android.ext.android.inject

class ScanResultValidExplanationDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentScanResultValidReasonBinding
    private val args: ScanResultValidExplanationDialogFragmentArgs by navArgs()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanResultValidReasonBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        args.qrResult.let {
            val personalDetails = personalDetailsUtil.getPersonalDetails(
                it.firstNameInitial,
                it.lastNameInitial,
                it.birthDay,
                it.birthMonth
            )
            binding.personalDetailsHolder.setPersonalDetails(personalDetails, true)
        }

    }
}