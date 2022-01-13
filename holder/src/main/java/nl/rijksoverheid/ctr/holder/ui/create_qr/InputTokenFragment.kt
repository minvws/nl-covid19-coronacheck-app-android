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
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
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
 */
abstract class InputTokenFragment : BaseFragment(R.layout.fragment_input_token) {

    private var _binding: FragmentInputTokenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InputTokenViewModel by stateViewModel(
        state = emptyState(),
    )

    private val dialogUtil: DialogUtil by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onButtonClickWithRetryAction() {
        fetchTestResults(binding)
    }

    private fun setCopies() {
        val data = getFragmentData()

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

        setCopies()

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
                binding.description.setText(getFragmentData().descriptionDeeplink)
            }

            binding.uniqueCodeText.setHint(
                if (Accessibility.screenReader(context)) {
                    getFragmentData().uniqueCodeInputHintScreenReader
                } else {
                    getFragmentData().uniqueCodeInputHint
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
                is TestResult.EmptyToken -> showTokenError(getFragmentData().noUniqueCodeEntered)
                is TestResult.InvalidToken -> showTokenError(R.string.commercial_test_error_invalid_code)
                is TestResult.UnknownTestProvider -> showTokenError(R.string.commercial_test_error_unknown_test_provider)
                is TestResult.NegativeTestResult -> showNegativeTestResult(it)
                is TestResult.NoNegativeTestResult -> {
                    navigateCouldNotCreateQr()
                }
                is TestResult.Pending -> {
                    navigateCouldNotCreateQr()
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
        getDeeplinkToken()?.let { token ->
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
                title = getString(getFragmentData().noCodeDialogTitle),
                descriptionData = DescriptionData(getFragmentData().noCodeDialogDescription),
            ))
        }
    }

    private fun showNegativeTestResult(result: TestResult.NegativeTestResult) {
        navigateMyEvents(result)
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

    fun getOriginType(remoteProtocol: RemoteProtocol): OriginType {
        return when (remoteProtocol) {
            is RemoteTestResult2 -> {
                OriginType.Test
            }
            is RemoteProtocol3 -> {
                if (remoteProtocol.events?.any { it is RemoteEventVaccinationAssessment } == true) {
                    OriginType.VaccinationAssessment
                } else {
                    OriginType.Test
                }
            } else -> {
                OriginType.Test
            }
        }
    }

    fun getYourEventsToolbarTitle(remoteProtocol: RemoteProtocol): Int {
        return when (remoteProtocol) {
            is RemoteTestResult2 -> {
                R.string.your_negative_test_results_toolbar
            }
            is RemoteProtocol3 -> {
                if (remoteProtocol.events?.any { it is RemoteEventVaccinationAssessment } == true) {
                    R.string.holder_event_vaccination_assessment_toolbar_title
                } else {
                    R.string.your_negative_test_results_toolbar
                }
            } else -> {
                R.string.your_negative_test_results_toolbar
            }
        }
    }

    abstract fun getFragmentData(): InputTokenFragmentData
    abstract fun navigateCouldNotCreateQr()
    abstract fun navigateMyEvents(result: TestResult.NegativeTestResult)
    abstract fun getDeeplinkToken(): String?
}
