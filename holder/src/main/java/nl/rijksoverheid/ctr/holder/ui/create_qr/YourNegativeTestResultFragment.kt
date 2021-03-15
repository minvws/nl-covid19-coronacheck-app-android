package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourNegativeTestResultsBinding
import nl.rijksoverheid.ctr.holder.usecase.SignedTestResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.scope.emptyState
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class YourNegativeTestResultFragment : BaseFragment(R.layout.fragment_your_negative_test_results) {

    //TODO depending on the graph and reuse we probably need to know if this is GGD or commercial
    private val appConfigUtil: AppConfigUtil by inject()
    private val viewModel: TestResultsViewModel by sharedViewModel(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_commercial_test),
                this
            )
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentYourNegativeTestResultsBinding.bind(view)

        val retrievedResult = viewModel.retrievedResult
        if (retrievedResult == null) {
            // restored from state, no result anymore
            findNavController().navigate(YourNegativeTestResultFragmentDirections.actionMyOverview())
        } else {
            retrievedResult.remoteTestResult.result?.let { result ->
                binding.rowSubtitle.text =
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(result.sampleDate.toEpochSecond()),
                        ZoneOffset.UTC
                    ).formatDateTime(requireContext())


                binding.info.setOnClickListener {
                    findNavController().navigate(
                        YourNegativeTestResultFragmentDirections.actionYourNegativeResultExplanation(
                            result.holder
                        )
                    )
                }

            }

            binding.rowPersonalDetails.text = getString(
                R.string.your_negative_test_results_row_personal_details,
                "${retrievedResult.personalDetails[0]} ${retrievedResult.personalDetails[1]} ${retrievedResult.personalDetails[2]} ${retrievedResult.personalDetails[3]}"
            )
        }

        // Catch back button to show modal instead
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackButton()
            }
        })

        binding.button.setOnClickListener {
            viewModel.saveTestResult()
        }

        viewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (requireActivity() as HolderMainActivity).presentLoading(it)
            binding.button.isEnabled = !it
        })

        viewModel.signedTestResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is SignedTestResult.Complete -> {
                    findNavController().navigate(
                        YourNegativeTestResultFragmentDirections.actionMyOverview()
                    )
                }
                is SignedTestResult.AlreadySigned -> {
                    findNavController().navigate(
                        CommercialTestCodeFragmentDirections.actionNoTestResult(
                            title = getString(R.string.test_result_already_signed_title),
                            description = getString(R.string.test_result_already_signed_description)
                        )
                    )
                }
                is SignedTestResult.ServerError -> {
                    presentError()
                }
                is SignedTestResult.NetworkError -> {
                    presentError()
                }
            }
        })
    }

    private fun handleBackButton() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.your_negative_test_results_backbutton_title))
            .setMessage(getString(R.string.your_negative_test_results_backbutton_message))
            .setPositiveButton(R.string.your_negative_test_results_backbutton_ok) { _, _ ->
                findNavController().navigate(
                    YourNegativeTestResultFragmentDirections.actionMyOverview()
                )
            }
            .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
            .show()
    }

}
