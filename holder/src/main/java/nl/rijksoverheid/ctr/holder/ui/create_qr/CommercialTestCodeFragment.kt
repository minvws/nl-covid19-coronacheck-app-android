package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.appconfig.AppConfigUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestCodeBinding
import nl.rijksoverheid.ctr.holder.ext.hideKeyboard
import nl.rijksoverheid.ctr.holder.ext.showKeyboard
import nl.rijksoverheid.ctr.holder.usecase.TestResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ViewModelOwner.Companion.from
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.scope.emptyState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CommercialTestCodeFragment : BaseFragment(R.layout.fragment_commercial_test_code) {

    private val viewModel: TestResultsViewModel by sharedViewModel(
        state = emptyState(),
        owner = {
            from(
                findNavController().getViewModelStoreOwner(R.id.nav_commercial_test),
                this
            )
        })

    private val appConfigUtil: AppConfigUtil by inject()
    private val navArgs: CommercialTestCodeFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentCommercialTestCodeBinding.bind(view)

        if (viewModel.verificationRequired) {
            showKeyboard(binding.verificationCodeText)
        } else {
            showKeyboard(binding.uniqueCodeText)
        }

        viewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (activity as HolderMainActivity).presentLoading(it)
            binding.button.isEnabled = !it
        })

        binding.uniqueCodeText.addTextChangedListener {
            viewModel.testCode = it?.toString()?.toUpperCase() ?: ""
        }

        binding.verificationCodeText.addTextChangedListener {
            viewModel.verificationCode = it?.toString() ?: ""
        }

        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.button.isEnabled = it.canRetrieveResult
            binding.uniqueCodeText.imeOptions =
                (if (it.verificationRequired) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEND)
            binding.verificationCodeInput.visibility =
                if (it.verificationRequired) View.VISIBLE else View.GONE
        }

        binding.uniqueCodeText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND && !viewModel.verificationRequired) {
                fetchTestResults(binding)
                true
            } else {
                false
            }
        }

        viewModel.testResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                TestResult.InvalidToken -> {
                    binding.uniqueCodeInput.error =
                        getString(R.string.commercial_test_error_invalid_code)
                }
                TestResult.NetworkError,
                TestResult.ServerError -> presentError()
                is TestResult.NegativeTestResult -> {
                    findNavController().navigate(CommercialTestCodeFragmentDirections.actionYourNegativeResult())
                }
                is TestResult.NoNegativeTestResult -> {
                    findNavController().navigate(
                        CommercialTestCodeFragmentDirections.actionNoTestResult(
                            title = getString(R.string.no_negative_test_result_title),
                            description = appConfigUtil.getStringWithTestValidity(R.string.no_negative_test_result_description)
                        )
                    )
                }
                is TestResult.Pending -> {
                    findNavController().navigate(
                        CommercialTestCodeFragmentDirections.actionNoTestResult(
                            title = getString(R.string.test_result_not_known_title),
                            description = getString(R.string.test_result_not_known_description)
                        )
                    )
                }
                TestResult.VerificationRequired -> {
                    // If we come here a second time, it means the inputted verification code is not valid
                    if (binding.verificationCodeText.text?.isNotEmpty() == true) {
                        binding.verificationCodeInput.error =
                            getString(R.string.commercial_test_error_invalid_combination)
                    }

                    binding.verificationCodeInput.requestFocus()
                }
            }
        })

        binding.button.setOnClickListener {
            fetchTestResults(binding)
        }

        // If a location token is set, automatically fill it in
        navArgs.locationToken?.let { token ->
            binding.uniqueCodeText.setText(token)
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    private fun fetchTestResults(binding: FragmentCommercialTestCodeBinding) {
        binding.verificationCodeInput.error = null
        binding.uniqueCodeInput.error = null
        viewModel.getTestResult()
        hideKeyboard()
    }
}
