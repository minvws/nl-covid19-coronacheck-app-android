/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.YourEventWidget
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class YourEventsFragment : Fragment(R.layout.fragment_your_events) {

    private val args: YourEventsFragmentArgs by navArgs()

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    private val yourEventsViewModel: YourEventsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentYourEventsBinding.bind(view)
        val type = args.type

        when (type) {
            is YourEventsFragmentType.Vaccination -> {
                presentVaccinations(
                    binding = binding,
                    remoteEvents = type.remoteEvents
                )
            }
            is YourEventsFragmentType.NegativeTest -> {
                presentNegativeTestResult(
                    binding = binding,
                    remoteEvents = type.remoteEvents,
                )
            }
            is YourEventsFragmentType.TestResult -> {
                presentTestResult(
                    binding = binding,
                    remoteTestResult = type.remoteTestResult,
                    remoteTestResultRawResponse = type.rawResponse
                )
            }
        }

        yourEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
            binding.bottom.setButtonEnabled(!it)
        })

        yourEventsViewModel.savedEvents.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(
                YourEventsFragmentDirections.actionMyOverview()
            )
        })
    }

    private fun presentNegativeTestResult(
        binding: FragmentYourEventsBinding,
        remoteEvents: Map<RemoteEventsNegativeTests, ByteArray>
    ) {
        binding.title.setText(R.string.your_negative_test_results_title)
        binding.description.setHtmlText(getString(R.string.your_negative_test_results_description))

        val lastNegativeTestResult = remoteEvents.keys.map { it.events }.flatten().firstOrNull()
        val holder = remoteEvents.keys.map { it.holder }.firstOrNull()

        if (holder != null && lastNegativeTestResult != null) {
            val personalDetails = personalDetailsUtil.getPersonalDetails(
                firstNameInitial = holder.firstName,
                lastNameInitial = holder.lastName,
                birthDay = holder.birthDate,
                birthMonth = holder.birthDate,
                includeBirthMonthNumber = false
            )

            val eventWidget = YourEventWidget(requireContext()).also {
                it.setContent(
                    title = getString(R.string.your_negative_test_results_row_title),
                    subtitle = getString(
                        R.string.your_negative_test_results_row_subtitle,
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(lastNegativeTestResult.getOffsetDateTime().toEpochSecond()),
                            ZoneOffset.UTC
                        ).formatDateTime(requireContext()),
                        lastNegativeTestResult.getOffsetDateTime().plusHours(
                            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                        ).formatDateTime(requireContext()),
                        "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
                    ),
                    infoClickListener = {
                        findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation())
                    }
                )
            }
            binding.eventsGroup.addView(eventWidget)

            // Catch back button to show modal instead
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
                OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.your_negative_test_results_backbutton_title))
                        .setMessage(getString(R.string.your_negative_test_results_backbutton_message))
                        .setPositiveButton(R.string.your_negative_test_results_backbutton_ok) { _, _ ->
                            findNavController().navigate(
                                YourEventsFragmentDirections.actionMyOverview()
                            )
                        }
                        .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
                        .show()
                }
            })

            // Handle button
            binding.bottom.setButtonClick {
                yourEventsViewModel.saveRemoteNegativeResultEvents(remoteEvents)
            }
        }

        binding.somethingWrongButton.setOnClickListener {
            findNavController().navigate(YourEventsFragmentDirections.actionShowSomethingWrong())
        }
    }

    private fun presentTestResult(
        binding: FragmentYourEventsBinding,
        remoteTestResult: RemoteTestResult,
        remoteTestResultRawResponse: ByteArray,
    ) {
        binding.title.setText(R.string.your_negative_test_results_title)
        binding.description.setHtmlText(getString(R.string.your_negative_test_results_description))

        remoteTestResult.result?.let { result ->
            val personalDetails = personalDetailsUtil.getPersonalDetails(
                firstNameInitial = result.holder.firstNameInitial,
                lastNameInitial = result.holder.lastNameInitial,
                birthDay = result.holder.birthDay,
                birthMonth = result.holder.birthMonth,
                includeBirthMonthNumber = false
            )

            val eventWidget = YourEventWidget(requireContext()).also {
                it.setContent(
                    title = getString(R.string.your_negative_test_results_row_title),
                    subtitle = getString(
                        R.string.your_negative_test_results_row_subtitle,
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(result.sampleDate.toEpochSecond()),
                            ZoneOffset.UTC
                        ).formatDateTime(requireContext()),
                        result.sampleDate.plusHours(
                            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                        ).formatDateTime(requireContext()),
                        "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
                    ),
                    infoClickListener = {
                        findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation())
                    }
                )
            }
            binding.eventsGroup.addView(eventWidget)

            // Catch back button to show modal instead
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
                OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.your_negative_test_results_backbutton_title))
                        .setMessage(getString(R.string.your_negative_test_results_backbutton_message))
                        .setPositiveButton(R.string.your_negative_test_results_backbutton_ok) { _, _ ->
                            findNavController().navigate(
                                YourEventsFragmentDirections.actionMyOverview()
                            )
                        }
                        .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
                        .show()
                }
            })

            // Handle button
            binding.bottom.setButtonClick {
                yourEventsViewModel.saveRemoteTestResult(
                    remoteTestResult = remoteTestResult,
                    rawResponse = remoteTestResultRawResponse
                )
            }
        }

        binding.somethingWrongButton.setOnClickListener {
            findNavController().navigate(YourEventsFragmentDirections.actionShowSomethingWrong())
        }
    }

    private fun presentVaccinations(
        binding: FragmentYourEventsBinding,
        remoteEvents: Map<RemoteEvents, ByteArray>
    ) {
        binding.title.visibility = View.GONE
        binding.description.text =
            getString(R.string.your_retrieved_vaccinations_description)

        remoteEvents.keys.map { it.events }.flatten()
            .sortedBy { it.getDate().toEpochDay() }
            .forEachIndexed { index, event ->
                val eventWidget = YourEventWidget(requireContext()).also {
                    it.setContent(
                        title = resources.getString(
                            R.string.retrieved_vaccination_title,
                            index + 1
                        ),
                        subtitle = resources.getString(
                            R.string.retrieved_vaccination_subtitle,
                            event.getDate()
                        ),
                        infoClickListener = {
                            findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation())
                        }
                    )
                }
                binding.eventsGroup.addView(eventWidget)
            }

        // Catch back button to show modal instead
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.retrieved_vaccinations_backbutton_title))
                    .setMessage(getString(R.string.retrieved_vaccinations_backbutton_message))
                    .setPositiveButton(R.string.your_negative_test_results_backbutton_ok) { _, _ ->
                        findNavController().navigate(
                            YourEventsFragmentDirections.actionMyOverview()
                        )
                    }
                    .setNegativeButton(R.string.your_negative_test_results_backbutton_cancel) { _, _ -> }
                    .show()
            }
        })

        // Handle button
        binding.bottom.setButtonClick {
            yourEventsViewModel.saveRemoteEvents(
                remoteEvents = remoteEvents
            )
        }

        // Hide something wrong button
        binding.somethingWrongButton.visibility = View.GONE
    }
}
