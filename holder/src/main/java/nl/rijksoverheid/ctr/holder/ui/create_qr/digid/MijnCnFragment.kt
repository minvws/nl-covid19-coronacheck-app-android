/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMijCnBinding
import nl.rijksoverheid.ctr.holder.launchUrl
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventProvider
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventsResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MijnCnFragment : DigiDFragment(R.layout.fragment_mij_cn) {

    private val getEventsViewModel: GetEventsViewModel by viewModel()
    private val dialogUtil: DialogUtil by inject()
    override fun onButtonClickWithRetryAction() {
        loginWithMijnCN()
    }

    override fun getFlow(): Flow {
        return HolderFlow.Vaccination
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMijCnBinding.bind(view)

        binding.requestDigidButton.bind(
            R.string.no_digid_nodigid_button_title,
            getString(R.string.no_digid_nodigid_button_description),
            onClick = {
                context?.launchUrl(getString(R.string.no_digid_url))
            })

        binding.mijncnButton.bind(
            R.string.no_digid_mijncn_button_title,
            getString(R.string.no_digid_mijncn_button_description),
            onClick = {
                onButtonClickWithRetryAction()
            })

        mijnCnViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.mijncnButton.root.isEnabled != it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.mijncnButton.root.isEnabled != it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })


        mijnCnViewModel.mijnCnResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DigidResult.Success -> {
                    getEventsViewModel.getEvents(
                        it.jwt,
                        OriginType.Vaccination,
                        mijnCN = true
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


        getEventsViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = getDialogTitleFromOriginType(OriginType.Vaccination),
                            message = getString(R.string.error_get_events_missing_events_dialog_description),
                            positiveButtonText = R.string.dialog_close,
                            positiveButtonCallback = {},
                            onDismissCallback = {

                            }
                        )
                    } else {
                        Timber.d("We got events!")
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
                       Timber.d("MEssed up missingevents")
                    }
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
                        it.isMijnCnMissingDataErrors() -> {
                            val errorCodeString = errorCodeStringFactory.get(
                                flow = getFlow(),
                                errorResults = listOf(it.errorResults.first())
                            )
                            presentError(
                                data = ErrorResultFragmentData(
                                    title = getString(R.string.event_provider_error_title),
                                    description = getString(R.string.mijncn_error_missing_data_description, errorCodeString),
                                    buttonTitle = getString(R.string.back_to_overview),
                                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
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

    }


    private fun navigateToYourEvents(
        signedEvents: List<SignedResponseWithModel<RemoteProtocol3>>,
        eventProviders: List<EventProvider> = emptyList(),
    ) {
        navigateSafety(
            MijnCnFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = signedEvents.map { signedModel -> signedModel.model to signedModel.rawResponse }
                        .toMap(),
                    originType = OriginType.Vaccination,
                    eventProviders = eventProviders,
                ),
                toolbarTitle = getCopyForOriginType(OriginType.Vaccination).toolbarTitle
            )
        )
    }

    private fun getCopyForOriginType(originType: OriginType): GetEventsFragmentCopy {
        when (originType) {
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


}