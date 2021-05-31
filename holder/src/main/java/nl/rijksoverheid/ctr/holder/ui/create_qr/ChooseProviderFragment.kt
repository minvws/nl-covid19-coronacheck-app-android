package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.ggd.GetGgdResultFragmentDirections
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.EventsResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
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

    private val androidUtil: AndroidUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    private val chooseProviderViewModel: ChooseProviderViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChooseProviderBinding.bind(view)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        }

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

        binding.providerCommercial.root.setAsAccessibilityButton()

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        chooseProviderViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success<RemoteEventsNegativeTests> -> {
                    findNavController().navigate(
                        GetGgdResultFragmentDirections.actionYourEvents(
                            type = YourEventsFragmentType.NegativeTest(
                                remoteEvents = it.signedModels.map { signedModel -> signedModel.model to signedModel.rawResponse }
                                    .toMap()
                            ),
                            toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title)
                        )
                    )
                }
                is EventsResult.NetworkError -> {
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
                is EventsResult.ServerError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_error_title,
                        message = getString(
                            R.string.dialog_error_message_with_error_code,
                            it.httpCode.toString()
                        ),
                        positiveButtonText = R.string.dialog_retry,
                        positiveButtonCallback = {
                            loginWithDigiD()
                        },
                        negativeButtonText = R.string.dialog_close
                    )
                }
            }
        })

        digidViewModel.accessTokenLiveData.observe(viewLifecycleOwner, EventObserver {
            chooseProviderViewModel.getEvents(it)
        })
    }
}