package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetVaccinationBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigidResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
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
class GetVaccinationFragment : DigiDFragment(R.layout.fragment_get_vaccination) {

    private val dialogUtil: DialogUtil by inject()
    private val getVaccinationViewModel: GetVaccinationViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGetVaccinationBinding.bind(view)

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getVaccinationViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getVaccinationViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success<RemoteEventsVaccinations> -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.missing_events_title,
                            message = getString(R.string.missing_events_description),
                            positiveButtonText = R.string.ok,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                findNavController().navigate(
                                    GetVaccinationFragmentDirections.actionYourEvents(
                                        type = YourEventsFragmentType.Vaccination(
                                            remoteEvents = it.signedModels.map { signedModel -> signedModel.model to signedModel.rawResponse }
                                                .toMap()
                                        ),
                                        toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title)
                                    )
                                )
                            }
                        )
                    } else {
                        findNavController().navigate(
                            GetVaccinationFragmentDirections.actionYourEvents(
                                type = YourEventsFragmentType.Vaccination(
                                    remoteEvents = it.signedModels.map { signedModel -> signedModel.model to signedModel.rawResponse }
                                        .toMap()
                                ),
                                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title)
                            )
                        )
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
                            GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                                toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                                title = getString(R.string.no_vaccinations_title),
                                description = getString(R.string.no_vaccinations_description)
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
                            toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                            title = getString(R.string.event_provider_error_title),
                            description = getString(R.string.event_provider_error_description)
                        )
                    )
                }
                is EventsResult.Error.CoronaCheckError.ServerError -> {
                    findNavController().navigate(
                        GetVaccinationFragmentDirections.actionCouldNotCreateQr(
                            toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
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
                    getVaccinationViewModel.getEvents(it.jwt)
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
}
