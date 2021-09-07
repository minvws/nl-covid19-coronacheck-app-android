package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestCodeBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.ext.hideKeyboard
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.ext.showKeyboard
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.scope.emptyState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */class CommercialTestCodeFragment : BaseFragment(R.layout.fragment_commercial_test_code) {

    private var _binding: FragmentCommercialTestCodeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommercialTestCodeViewModel by stateViewModel(
        state = emptyState(),
    )

    private val dialogUtil: DialogUtil by inject()

    private val navArgs: CommercialTestCodeFragmentArgs by navArgs()

    override fun onButtonClickWithRetryAction() {
        fetchTestResults(binding)
    }

    override fun getFlow(): Flow {
        return HolderFlow.CommercialTest
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentCommercialTestCodeBinding.bind(view)

        binding.uniqueCodeText.filters = arrayOf(InputFilter.AllCaps())
        binding.uniqueCodeText.addTextChangedListener {
            viewModel.testCode = it?.toString()?.toUpperCase() ?: ""
        }

        binding.verificationCodeText.addTextChangedListener {
            viewModel.verificationCode = it?.toString() ?: ""
        }

        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.bottom.setButtonEnabled(it.canRetrieveResult)
            binding.uniqueCodeText.imeOptions =
                (if (it.verificationRequired) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEND)
            binding.verificationCodeInput.visibility =
                if (it.verificationRequired) View.VISIBLE else View.GONE
            binding.noVerificationRecievedBtn.visibility =
                if (it.verificationRequired) View.VISIBLE else View.GONE
            binding.noTokenReceivedBtn.visibility =
                if (!it.verificationRequired) View.VISIBLE else View.GONE

            if (it.fromDeeplink && it.verificationRequired) {
                binding.uniqueCodeInput.isVisible = false
                binding.noTokenReceivedBtn.isVisible = false
                binding.description.setText(R.string.commercial_test_verification_code_description_deeplink)
            }

            binding.uniqueCodeText.setHint(
                if (Accessibility.screenReader(context)) {
                    R.string.commercial_test_unique_code_hint_screenreader
                } else {
                    R.string.commercial_test_unique_code_hint
                }
            )
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
                is TestResult.InvalidToken -> {
                    binding.uniqueCodeInput.error =
                        getString(R.string.commercial_test_error_invalid_code)
                    binding.verificationCodeInput.isVisible = false
                }
                is TestResult.NegativeTestResult -> {
                    when (it.remoteTestResult) {
                        is RemoteTestResult2 -> {
                            findNavController().navigate(
                                CommercialTestCodeFragmentDirections.actionYourEvents(
                                    type = YourEventsFragmentType.TestResult2(
                                        remoteTestResult = it.remoteTestResult,
                                        rawResponse = it.signedResponseWithTestResult.rawResponse
                                    ),
                                    toolbarTitle = getString(R.string.commercial_test_type_title)
                                )
                            )
                        }
                        is RemoteProtocol3 -> {
                            findNavController().navigate(
                                CommercialTestCodeFragmentDirections.actionYourEvents(
                                    type = YourEventsFragmentType.RemoteProtocol3Type(mapOf(it.remoteTestResult to it.signedResponseWithTestResult.rawResponse),
                                    originType = OriginType.Test),
                                    toolbarTitle = getString(R.string.commercial_test_type_title)
                                )
                            )
                        }
                    }
                }
                is TestResult.NoNegativeTestResult -> {
                    findNavController().navigate(
                        CommercialTestCodeFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.commercial_test_type_title),
                            title = getString(R.string.no_negative_test_result_title),
                            description = getString(R.string.no_negative_test_result_description),
                            buttonTitle = getString(R.string.back_to_overview)
                        )
                    )
                }
                is TestResult.Pending -> {
                    findNavController().navigate(
                        CommercialTestCodeFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.commercial_test_type_title),
                            title = getString(R.string.test_result_not_known_title),
                            description = getString(R.string.test_result_not_known_description),
                            buttonTitle = getString(R.string.back_to_overview)
                        )
                    )
                }
                is TestResult.VerificationRequired -> {
                    // If we come here a second time, it means the inputted verification code is not valid
                    if (binding.verificationCodeText.text?.isNotEmpty() == true) {
                        binding.verificationCodeInput.error =
                            getString(R.string.commercial_test_error_invalid_combination)
                    }
                    binding.verificationCodeInput.requestFocus()
                }
                is TestResult.Error -> {
                    presentError(
                        errorResult = it.errorResult
                    )
                }
            }
        })

        // Show dialog to send verification code again
        binding.noVerificationRecievedBtn.setOnClickListener {
            dialogUtil.presentDialog(
                context = requireContext(),
                title = R.string.dialog_verification_code_title,
                message = getString(R.string.dialog_verification_code_message),
                positiveButtonText = R.string.dialog_verification_code_positive_button,
                positiveButtonCallback = {
                    viewModel.sendVerificationCode()
                },
                negativeButtonText = R.string.dialog_close
            )
        }

        binding.bottom.setButtonClick {
            onButtonClickWithRetryAction()
        }

        // If a location token is set, automatically fill it in. Else we show the keyboard focussing on first code input field
        navArgs.token?.let { token ->
            // Only run this once. If the token has been handled once don't try to retrieve a testresult automatically again.
            if (viewModel.testCode.isEmpty()) {
                binding.uniqueCodeText.setText(token)
                fetchTestResults(binding, fromDeeplink = true)
            }
        } ?: showKeyboard(binding.uniqueCodeText)

        viewModel.loading.observe(viewLifecycleOwner, EventObserver {
            if (!viewModel.fromDeeplink) {
                (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
            } else {
                // Show different loading state when loading from deeplink
                binding.loadingOverlay.isVisible = it
            }
        })

        binding.noTokenReceivedBtn.setOnClickListener {
            navigateSafety(R.id.nav_commercial_test_code,
                CommercialTestCodeFragmentDirections.actionNoCode()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTestResults(
        binding: FragmentCommercialTestCodeBinding,
        fromDeeplink: Boolean = false
    ) {
        binding.verificationCodeInput.error = null
        binding.uniqueCodeInput.error = null
        viewModel.getTestResult(fromDeeplink)
    }
}
