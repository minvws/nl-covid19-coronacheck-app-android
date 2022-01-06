package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentInputTokenBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.ext.hideKeyboard
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
 */class InputTokenFragment : BaseFragment(R.layout.fragment_input_token) {

    private var _binding: FragmentInputTokenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InputTokenViewModel by stateViewModel(
        state = emptyState(),
    )

    private val dialogUtil: DialogUtil by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    private val navArgs: InputTokenFragmentArgs by navArgs()

    override fun onButtonClickWithRetryAction() {
        fetchTestResults(binding)
    }

    override fun getFlow(): Flow {
        return when (navArgs.data) {
            InputTokenFragmentData.CommercialTest -> HolderFlow.CommercialTest
            InputTokenFragmentData.VisitorPass -> HolderFlow.VaccinationAssessment
        }
    }

    private fun setCopies(data: InputTokenFragmentData) {
        binding.description.text = getString(data.description)
        binding.uniqueCodeInput.hint = getString(data.uniqueCodeInputHeader)
        binding.noTokenReceivedBtn.text = getString(data.noCodeText)
        binding.bottom.setButtonText(getString(data.buttonText))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentInputTokenBinding.bind(view)

        binding.uniqueCodeText.filters = arrayOf(InputFilter.AllCaps())
        binding.uniqueCodeText.addTextChangedListener {
            viewModel.testCode = it?.toString()?.uppercase() ?: ""
        }

        binding.verificationCodeText.addTextChangedListener {
            if (binding.verificationCodeInput.isVisible) {
                viewModel.verificationCode = it.toString().takeIf { it.isNotEmpty() }
            }
        }

        setCopies(navArgs.data)

        viewModel.viewState.observe(viewLifecycleOwner) {
            binding.uniqueCodeText.imeOptions =
                (if (it.verificationRequired) EditorInfo.IME_ACTION_NEXT else EditorInfo.IME_ACTION_SEND)
            binding.verificationCodeInput.visibility =
                if (it.verificationRequired) View.VISIBLE else View.GONE
            binding.noVerificationRecievedBtn.visibility =
                if (it.verificationRequired) View.VISIBLE else View.GONE
            binding.noTokenReceivedBtn.visibility =
                if (!it.verificationRequired) View.VISIBLE else View.GONE

            // Start with empty text for possible empty field error when field is visible
            if (it.verificationRequired && viewModel.verificationCode == null) {
                viewModel.verificationCode = ""
            }

            if (it.fromDeeplink && it.verificationRequired) {
                binding.uniqueCodeInput.isVisible = false
                binding.noTokenReceivedBtn.isVisible = false
                binding.description.setText(navArgs.data.descriptionDeeplink)
            }

            binding.uniqueCodeText.setHint(
                if (Accessibility.screenReader(context)) {
                    navArgs.data.uniqueCodeInputHintScreenReader
                } else {
                    navArgs.data.uniqueCodeInputHint
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
                is TestResult.EmptyToken -> showTokenError(navArgs.data.noUniqueCodeEntered)
                is TestResult.InvalidToken -> showTokenError(R.string.commercial_test_error_invalid_code)
                is TestResult.UnknownTestProvider -> showTokenError(R.string.commercial_test_error_unknown_test_provider)
                is TestResult.NegativeTestResult -> showNegativeTestResult(it)
                is TestResult.NoNegativeTestResult -> {
                    findNavController().navigate(
                        InputTokenFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(navArgs.data.noResultScreenToolbarTitle),
                            title = getString(navArgs.data.noResultScreenTitle),
                            description = getString(navArgs.data.noResultScreenDescription),
                            buttonTitle = getString(R.string.back_to_overview)
                        )
                    )
                }
                is TestResult.Pending -> {
                    findNavController().navigate(
                        InputTokenFragmentDirections.actionCouldNotCreateQr(
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
                        binding.verificationCodeInput.error = getString(R.string.commercial_test_error_invalid_combination)
                    }
                    binding.verificationCodeInput.requestFocus()
                }
                is TestResult.EmptyVerificationCode -> {
                    binding.verificationCodeInput.error = getString(R.string.commercial_test_error_empty_verification_code)
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
            infoFragmentUtil.presentAsBottomSheet(childFragmentManager, InfoFragmentData.TitleDescription(
                title = getString(navArgs.data.noCodeDialogTitle),
                descriptionData = DescriptionData(navArgs.data.noCodeDialogDescription),
            ))
        }
    }

    private fun showNegativeTestResult(result: TestResult.NegativeTestResult) {
        when (result.remoteTestResult) {
            is RemoteTestResult2 -> {
                findNavController().navigate(
                    InputTokenFragmentDirections.actionYourEvents(
                        type = YourEventsFragmentType.TestResult2(
                            remoteTestResult = result.remoteTestResult,
                            rawResponse = result.signedResponseWithTestResult.rawResponse
                        ),
                        toolbarTitle = getString(R.string.your_negative_test_results_toolbar)
                    )
                )
            }
            is RemoteProtocol3 -> {
                findNavController().navigate(
                    InputTokenFragmentDirections.actionYourEvents(
                        type = YourEventsFragmentType.RemoteProtocol3Type(
                            mapOf(result.remoteTestResult to result.signedResponseWithTestResult.rawResponse),
                            originType = if (navArgs.data is InputTokenFragmentData.CommercialTest) OriginType.Test else OriginType.VaccinationAssessment,
                            fromCommercialTestCode = true
                        ),
                        toolbarTitle = getString(navArgs.data.yourEventsToolbarTitle),
                    )
                )
            }
        }
    }

    private fun showTokenError(@StringRes errorMessageRes: Int) {
        binding.uniqueCodeInput.error = getString(errorMessageRes)
        binding.verificationCodeInput.isVisible = false
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
        binding: FragmentInputTokenBinding,
        fromDeeplink: Boolean = false
    ) {
        binding.verificationCodeInput.error = null
        binding.uniqueCodeInput.error = null
        viewModel.getTestResult(fromDeeplink)
    }
}
