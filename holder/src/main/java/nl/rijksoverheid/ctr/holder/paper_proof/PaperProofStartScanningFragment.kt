/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.paper_proof

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainActivityViewModel
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofStartScanningBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PaperProofStartScanningFragment : Fragment(R.layout.fragment_paper_proof_start_scanning) {

    private val holderMainActivityViewModel: HolderMainActivityViewModel by sharedViewModel()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPaperProofStartScanningBinding.bind(view)

        holderMainActivityViewModel.navigateLiveData.observe(viewLifecycleOwner, EventObserver {
            findNavControllerSafety()?.navigate(it)
        })

        binding.button.setOnClickListener {
            infoFragmentUtil.presentAsBottomSheet(
                fragmentManager = childFragmentManager,
                data = InfoFragmentData.TitleDescription(
                    title = getString(R.string.holder_paperproof_whichProofsCanBeUsed_title),
                    descriptionData = DescriptionData(
                        htmlText = R.string.holder_paperproof_whichProofsCanBeUsed_body
                    )
                )
            )
        }

        binding.bottom.setButtonClick {
            openScanner()
        }
    }

    private fun openScanner() {
        try {
            val cameraManager =
                requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (cameraManager.cameraIdList.isNotEmpty()) {
                Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                    .navigate(R.id.action_paper_proof_qr_scanner)
            } else {
                showNoCameraError()
            }
        } catch (exception: CameraAccessException) {
            showNoCameraError()
        }
    }

    private fun showNoCameraError() {
        findNavControllerSafety()?.navigate(
            R.id.action_error_result,
            ErrorResultFragment.getBundle(
                ErrorResultFragmentData(
                    title = getString(R.string.add_paper_proof_no_camera_error_header),
                    description = getString(R.string.add_paper_proof_no_camera_error_description),
                    buttonTitle = getString(R.string.back_to_overview),
                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                )
            )
        )
    }
}
