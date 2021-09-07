package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChooseProviderFragment : DigiDFragment(R.layout.fragment_choose_provider) {

    private val dialogUtil: DialogUtil by inject()

    private val getEventsViewModel: GetEventsViewModel by viewModel()

    override fun onButtonClickWithRetryAction() {
        loginWithDigiD()
    }

    override fun getFlow(): Flow {
        return HolderFlow.CommercialTest
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChooseProviderBinding.bind(view)

        binding.providerCommercial.bind(
            R.string.choose_provider_commercial_title,
            null
        ) {
            findNavController().navigate(ChooseProviderFragmentDirections.actionCommercialTestCode())
        }

        binding.providerGgd.bind(
            R.string.choose_provider_ggd_title,
            getString(R.string.choose_provider_ggd_subtitle)
        ) {
            onButtonClickWithRetryAction()
        }

        binding.notYetTested.setOnClickListener {
            findNavController().navigate(ChooseProviderFragmentDirections.actionNotYetTested())
        }

        binding.providerCommercial.root.setAsAccessibilityButton()

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = getDialogTitleFromOriginType(OriginType.Test),
                            message = getString(R.string.error_get_events_missing_events_dialog_description),
                            positiveButtonText = R.string.dialog_close,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                navigateToYourEvents(
                                    signedEvents = it.signedModels
                                )
                            }
                        )
                    } else {
                        navigateToYourEvents(it.signedModels)
                    }
                }
                is EventsResult.HasNoEvents -> {
                    if (it.missingEvents) {
                        presentError(
                            data = ErrorResultFragmentData(
                                title = getString(R.string.error_get_events_no_events_title),
                                description = getString(R.string.error_get_events_http_error_description, getErrorCodes(it.errorResults)),
                                buttonTitle = getString(R.string.back_to_overview),
                                ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview),
                                urlData = ErrorResultFragmentData.UrlData(
                                    urlButtonTitle = getString(R.string.error_something_went_wrong_outage_button),
                                    urlButtonUrl = getString(R.string.error_something_went_wrong_outage_button_url)
                                ),
                            )
                        )
                    } else {
                        findNavController().navigate(
                            ChooseProviderFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.commercial_test_type_title),
                                title = getString(R.string.no_test_results_title),
                                description = getString(R.string.no_test_results_description),
                                buttonTitle = getString(R.string.back_to_overview)
                            )
                        )
                    }
                }
                is EventsResult.Error -> {
                    presentError(
                        data = ErrorResultFragmentData(
                            title = getString(R.string.error_something_went_wrong_title),
                            description = getString(R.string.error_get_events_http_error_description, getErrorCodes(it.errorResults)),
                            buttonTitle = getString(R.string.back_to_overview),
                            ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview),
                            urlData = ErrorResultFragmentData.UrlData(
                                urlButtonTitle = getString(R.string.error_something_went_wrong_outage_button),
                                urlButtonUrl = getString(R.string.error_something_went_wrong_outage_button_url)
                            ),
                        )
                    )
                }
            }
        })

        digidViewModel.digidResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DigidResult.Success -> {
                    getEventsViewModel.getEvents(
                        jwt = it.jwt,
                        originType = OriginType.Test)
                }
                is DigidResult.Failed -> {
                    presentError(it.errorResult)
                }
                DigidResult.Cancelled -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.digid_login_cancelled_title,
                        message = getString(R.string.digid_login_cancelled_description),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
            }
        })
    }

    private fun navigateToYourEvents(signedEvents: List<SignedResponseWithModel<RemoteProtocol3>>) {
        findNavController().navigate(
            ChooseProviderFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = signedEvents.map { signedModel -> signedModel.model to signedModel.rawResponse }
                        .toMap(),
                    originType = OriginType.Test
                ),
                toolbarTitle = getString(R.string.commercial_test_type_title)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }
}