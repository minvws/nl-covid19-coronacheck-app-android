package nl.rijksoverheid.ctr.verifier.scaninstructions

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.design.FullScreenDialogFragment
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.DialogScanInstructionsBinding
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionsDialogFragment : FullScreenDialogFragment(R.layout.dialog_scan_instructions) {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val EXTRA_LAUNCH_SCANNER = "LAUNCH_SCANNER"
    }

    private val appConfigUtil: AppConfigUtil by inject()
    private val args: ScanInstructionsDialogFragmentArgs by navArgs()

    override fun getAnimationStyle(): AnimationStyle {
        return AnimationStyle.SlideFromBottom
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = DialogScanInstructionsBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        GroupAdapter<GroupieViewHolder>()
            .run {
                addAll(
                    listOf(
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_1_title,
                            description = getString(R.string.scan_instructions_1_description),
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_2_title,
                            description = getString(R.string.scan_instructions_2_description),
                            image = R.drawable.illustration_scan_instruction_2
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_3_title,
                            description = getString(R.string.scan_instructions_3_description),
                            image = R.drawable.illustration_scan_instruction_3
                        ),
                        ScanInstructionAdapterItem(
                            title = R.string.scan_instructions_4_title,
                            description = appConfigUtil.getStringWithTestValidity(R.string.scan_instructions_4_description)
                        )
                    )
                )
                binding.recyclerView.adapter = this
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setFragmentResult(REQUEST_KEY, bundleOf(EXTRA_LAUNCH_SCANNER to args.openScannerOnBack))
    }
}
