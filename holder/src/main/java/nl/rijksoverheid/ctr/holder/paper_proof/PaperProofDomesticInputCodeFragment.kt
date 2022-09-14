/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.paper_proof

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentPaperProofDomesticInputCodeBinding
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticCodeResult
import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticResult
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.hideKeyboard
import nl.rijksoverheid.ctr.shared.ext.showKeyboard
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.androidx.viewmodel.scope.emptyState

class PaperProofDomesticInputCodeFragment : BaseFragment(R.layout.fragment_paper_proof_domestic_input_code) {

    private var _binding: FragmentPaperProofDomesticInputCodeBinding? = null
    private val binding get() = _binding!!
    private val args: PaperProofDomesticInputCodeFragmentArgs by navArgs()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    private val viewModel: PaperProofDomesticInputCodeViewModel by stateViewModel(
        state = emptyState()
    )

    override fun onButtonClickWithRetryAction() {
        viewModel.validateProof(
            qrContent = args.qrContent,
            couplingCode = binding.codeInputText.text.toString()
        )
    }

    override fun getFlow(): Flow {
        return HolderFlow.HkviScan
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPaperProofDomesticInputCodeBinding.bind(view)
        showKeyboard(binding.codeInputText)

        viewModel.loading.observe(viewLifecycleOwner) {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
            binding.bottom.setButtonEnabled(!it)
        }

        viewModel.validateCodeLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is PaperProofDomesticCodeResult.Valid -> {
                    onButtonClickWithRetryAction()
                }
                is PaperProofDomesticCodeResult.Empty -> {
                    binding.codeInput.error =
                        getString(R.string.add_paper_proof_input_empty)
                    binding.codeInput.isErrorEnabled = true
                    binding.codeInput.setErrorIconDrawable(R.drawable.ic_error)
                }
                is PaperProofDomesticCodeResult.Invalid -> {
                    binding.codeInput.error =
                        getString(R.string.add_paper_proof_input_invalid)
                    binding.codeInput.isErrorEnabled = true
                    binding.codeInput.setErrorIconDrawable(R.drawable.ic_error)
                }
            }
        })

        viewModel.validateProofLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is PaperProofDomesticResult.Valid -> {
                    findNavControllerSafety()?.navigate(PaperProofDomesticInputCodeFragmentDirections.actionYourEvents(
                        toolbarTitle = getString(R.string.your_dcc_event_toolbar_title),
                        type = YourEventsFragmentType.DCC(
                            remoteEvent = it.remoteEvent,
                            eventGroupJsonData = it.eventGroupJsonData,
                            originType = OriginType.fromTypeString(it.remoteEvent.events!!.first().type!!)
                        ),
                        flow = HolderFlow.HkviScan
                    ))
                }
                is PaperProofDomesticResult.Invalid.BlockedQr -> {
                    presentError(
                        data = ErrorResultFragmentData(
                            title = getString(R.string.add_paper_proof_limit_reached_paper_proof_title),
                            description = getString(R.string.add_paper_proof_limit_reached_paper_proof_description),
                            buttonTitle = getString(R.string.back_to_overview),
                            ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                        )
                    )
                }
                is PaperProofDomesticResult.Invalid.RejectedQr -> {
                    presentError(
                        data = ErrorResultFragmentData(
                            title = getString(R.string.add_paper_proof_invalid_combination_title),
                            description = getString(R.string.add_paper_proof_invalid_combination_),
                            buttonTitle = getString(R.string.back_to_overview),
                            ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                        )
                    )
                }
                is PaperProofDomesticResult.Invalid.Error -> {
                    presentError(
                        errorResult = it.errorResult
                    )
                }
            }
        })

        binding.noLetterCombinationBtn.setOnClickListener {
            infoFragmentUtil.presentFullScreen(
                currentFragment = this,
                toolbarTitle = getString(R.string.add_paper_proof_title),
                data = InfoFragmentData.TitleDescriptionWithButton(
                    title = getString(R.string.no_letter_combination_dialog_title),
                    descriptionData = DescriptionData(R.string.no_letter_combination_dialog_description),
                    secondaryButtonData = ButtonData.NavigationButton(
                        text = getString(R.string.add_paper_proof_self_printed_goto_add_proof_button),
                        navigationActionId = R.id.action_choose_proof_type
                    )
                )
            )
        }

        binding.bottom.setButtonClick {
            binding.codeInput.error = null
            viewModel.validateCode(binding.codeInputText.text.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as? HolderMainFragment)?.presentLoading(false)
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }
}
