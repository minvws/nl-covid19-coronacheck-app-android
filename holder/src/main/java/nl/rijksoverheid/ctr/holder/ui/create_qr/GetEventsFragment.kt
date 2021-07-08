package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetEventsBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetEventsFragment: DigiDFragment(R.layout.fragment_get_events) {

    private val args: GetEventsFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()
    private val getEventsViewModel: GetEventsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGetEventsBinding.bind(view)

        val copy = getCopyForOriginType()
        binding.title.text = copy.title
        binding.description.setHtmlText(
            htmlText = copy.description,
            htmlLinksEnabled = true
        )

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
                            title = R.string.missing_events_title,
                            message = getString(R.string.missing_events_description),
                            positiveButtonText = R.string.ok,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                navigateToYourEvents(
                                    signedEvents = it.signedModels
                                )
                            }
                        )
                    } else {
                        navigateToYourEvents(
                            signedEvents = it.signedModels
                        )
                    }
                }
                is EventsResult.HasNoEvents -> {
                    if (it.missingEvents) {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = copy.toolbarTitle,
                                title = getString(R.string.missing_events_title),
                                description = getString(R.string.missing_events_description)
                            )
                        )
                    } else {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = copy.toolbarTitle,
                                title = copy.hasNoEventsTitle,
                                description = copy.hasNoEventsDescription
                            )
                        )
                    }
                }
                is EventsResult.Error.NetworkError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_no_internet_connection_title,
                        message = getString(R.string.dialog_no_internet_connection_description),
                        positiveButtonText = R.string.dialog_retry,
                        positiveButtonCallback = {
                            loginWithDigiD()
                        },
                        negativeButtonText = R.string.dialog_close
                    )
                }
                is EventsResult.Error.EventProviderError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = copy.toolbarTitle,
                            title = getString(R.string.event_provider_error_title),
                            description = getString(R.string.event_provider_error_description)
                        )
                    )
                }
                is EventsResult.Error.CoronaCheckError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = copy.toolbarTitle,
                            title = getString(R.string.coronacheck_error_title),
                            description = getString(R.string.coronacheck_error_description, it.httpCode.toString())
                        )
                    )
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
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.digid_login_failed_title,
                        message = getString(R.string.digid_login_failed_description),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
            }
        })

        binding.button.setOnClickListener {
            loginWithDigiD()
        }

        binding.noDigidButton.setOnClickListener {
            getString(R.string.no_digid_url).launchUrl(requireContext())
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

    private fun navigateToYourEvents(signedEvents: List<SignedResponseWithModel<RemoteProtocol3>>) {
        findNavController().navigate(
            GetVaccinationFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = signedEvents.map { signedModel -> signedModel.model to signedModel.rawResponse }
                        .toMap(),
                    originType = args.originType
                ),
                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title)
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