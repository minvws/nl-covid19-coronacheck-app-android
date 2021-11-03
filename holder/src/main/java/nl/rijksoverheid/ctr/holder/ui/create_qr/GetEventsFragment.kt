package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetEventsBinding
import nl.rijksoverheid.ctr.holder.launchUrl
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetEventsFragment : DigiDFragment(R.layout.fragment_get_events) {

    private val args: GetEventsFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()

    private val getEventsViewModel: GetEventsViewModel by viewModel()

    override fun onButtonClickWithRetryAction() {
        loginWithDigiD()
    }

    override fun getFlow(): Flow {
        return when (args.originType) {
            OriginType.Recovery -> HolderFlow.Recovery
            OriginType.Test -> HolderFlow.DigidTest
            OriginType.Vaccination -> HolderFlow.Vaccination
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGetEventsBinding.bind(view)

        val copy = getCopyForOriginType()
        binding.title.text = copy.title
        binding.description.setHtmlText(copy.description, htmlLinksEnabled = true)

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.button.isEnabled = !it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.button.isEnabled = !it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = getDialogTitleFromOriginType(args.originType),
                            message = getString(R.string.error_get_events_missing_events_dialog_description),
                            positiveButtonText = R.string.dialog_close,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                navigateToYourEvents(
                                    signedEvents = it.signedModels,
                                    eventProviders = it.eventProviders,
                                )
                            }
                        )
                    } else {
                        navigateToYourEvents(
                            signedEvents = it.signedModels,
                            eventProviders = it.eventProviders,
                        )
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
                            GetEventsFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = copy.toolbarTitle,
                                title = copy.hasNoEventsTitle,
                                description = copy.hasNoEventsDescription,
                                buttonTitle = getString(R.string.back_to_overview)
                            )
                        )
                    }
                }
                is EventsResult.CannotCreateRecovery -> {
                    presentError(
                        data = ErrorResultFragmentData(
                            title = getString(R.string.cannot_create_recovery_proof_title),
                            description = getString(R.string.cannot_create_recovery_proof_description, it.validityDays.toString()),
                            buttonTitle = getString(R.string.back_to_overview),
                            ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview),
                        )
                    )
                }
                is EventsResult.Error -> {
                    when {
                        it.accessTokenSessionExpiredError() -> {
                            presentError(
                                data = ErrorResultFragmentData(
                                    title = getString(R.string.error_access_tokens_session_expired_title),
                                    description = getString(R.string.error_access_tokens_Session_expired_description),
                                    buttonTitle = getString(R.string.error_access_tokens_session_expired_button),
                                    ErrorResultFragmentData.ButtonAction.PopBackStack
                                )
                            )
                        }
                        it.accessTokenNoBsn() -> {
                            presentError(
                                data = ErrorResultFragmentData(
                                    title = getString(R.string.error_access_tokens_no_bsn_title),
                                    description = getString(R.string.error_access_tokens_no_bsn_description),
                                    buttonTitle = getString(R.string.back_to_overview),
                                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                                )
                            )
                        }
                        it.unomiOrEventErrors() -> {
                                presentError(
                                    it.errorResults.first(),
                                    getString(
                                        R.string.error_get_events_http_error_description,
                                        getErrorCodes(it.errorResults)
                                    )
                                )
                        }
                        else -> {
                            presentError(it.errorResults.first())
                        }
                    }
                }
            }
        })

        digidViewModel.digidResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DigidResult.Success -> {
                    getEventsViewModel.getEvents(
                        it.jwt,
                        args.originType
                    )
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

        binding.button.setOnClickListener {
            onButtonClickWithRetryAction()
        }

        binding.noDigidButton.setOnClickListener {
            context?.launchUrl(getString(R.string.no_digid_url))
        }
    }

    private fun getCopyForOriginType(): GetEventsFragmentCopy {
        when (args.originType) {
            is OriginType.Test -> {
                TODO("This logic is currently in ChooseProviderFragment but should be migrated here")
            }
            is OriginType.Vaccination -> {
                return GetEventsFragmentCopy(
                    title = getString(R.string.get_vaccination_title),
                    description = getString(R.string.get_vaccination_description),
                    toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                    hasNoEventsTitle = getString(R.string.no_vaccinations_title),
                    hasNoEventsDescription = getString(R.string.no_vaccinations_description)
                )
            }
            is OriginType.Recovery -> {
                return GetEventsFragmentCopy(
                    title = getString(R.string.get_recovery_title),
                    description = getString(R.string.get_recovery_description),
                    toolbarTitle = getString(R.string.your_positive_test_toolbar_title),
                    hasNoEventsTitle = getString(R.string.no_positive_test_result_title),
                    hasNoEventsDescription = getString(R.string.no_positive_test_result_description)
                )
            }
        }
    }

    private fun navigateToYourEvents(
        signedEvents: List<SignedResponseWithModel<RemoteProtocol3>>,
        eventProviders: List<EventProvider> = emptyList(),
    ) {
        navigateSafety(
            GetEventsFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = signedEvents.map { signedModel -> signedModel.model to signedModel.rawResponse }
                        .toMap(),
                    originType = args.originType,
                    eventProviders = eventProviders,
                ),
                toolbarTitle = getCopyForOriginType().toolbarTitle
            )
        )
    }
}

data class GetEventsFragmentCopy(
    val title: String,
    val description: String,
    val toolbarTitle: String,
    val hasNoEventsTitle: String,
    val hasNoEventsDescription: String
)