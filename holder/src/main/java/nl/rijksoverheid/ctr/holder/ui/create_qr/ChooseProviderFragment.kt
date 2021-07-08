package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
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
            loginWithDigiD()
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
                            title = R.string.missing_events_title,
                            message = getString(R.string.missing_events_description),
                            positiveButtonText = R.string.ok,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                navigateToYourEvents(it.signedModels)
                            }
                        )
                    } else {
                        navigateToYourEvents(it.signedModels)
                    }
                }
                is EventsResult.HasNoEvents -> {
                    if (it.missingEvents) {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                                title = getString(R.string.missing_events_title),
                                description = getString(R.string.missing_events_description)
                            )
                        )
                    } else {
                        findNavController().navigate(
                            ChooseProviderFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.commercial_test_type_title),
                                title = getString(R.string.no_test_results_title),
                                description = getString(R.string.no_test_results_description)
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
                            toolbarTitle = getString(R.string.commercial_test_type_title),
                            title = getString(R.string.event_provider_error_title),
                            description = getString(R.string.event_provider_error_description)
                        )
                    )
                }
                is EventsResult.Error.CoronaCheckError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.commercial_test_type_title),
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
                        jwt = it.jwt,
                        originType = OriginType.Test)
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
    }

    private fun navigateToYourEvents(signedEvents: List<SignedResponseWithModel<RemoteProtocol3>>) {
        findNavController().navigate(
            GetVaccinationFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = signedEvents.map { signedModel -> signedModel.model to signedModel.rawResponse }
                        .toMap(),
                    originType = OriginType.Test
                ),
                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title)
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }
}