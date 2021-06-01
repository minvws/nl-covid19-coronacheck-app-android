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
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.YourEventWidget
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Exception
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

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

        remoteEvents.keys.forEach { negativeTests ->
            val fullName =
                "${negativeTests.holder?.lastName}, ${negativeTests.holder?.firstName}"

            negativeTests.events?.forEach { event ->

                val testType = event.negativeTest?.type ?: ""
                val testLocation = event.negativeTest?.facility ?: ""

                val testDate = event.negativeTest?.sampleDate?.let { sampleDate ->
                    sampleDate.formatDateTime(requireContext())
                } ?: ""

                val resultDate = event.negativeTest?.resultDate?.let { sampleDate ->
                    sampleDate.formatDateTime(requireContext())
                } ?: ""

                val validUntil = event.negativeTest?.sampleDate?.let { sampleDate ->
                    OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(sampleDate.toEpochSecond()),
                        ZoneOffset.UTC
                    ).plusHours(
                        cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                    ).formatDateTime(requireContext())
                } ?: ""

                val birthDate = negativeTests.holder?.birthDate?.let { birthDate ->
                    try {
                        LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
                    } catch (e: Exception) {
                        ""
                    }
                } ?: ""

                val eventWidget = YourEventWidget(requireContext()).also {
                    it.setContent(
                        title = getString(R.string.your_negative_test_results_row_title),
                        subtitle = getString(
                            R.string.your_negative_test_3_0_results_row_subtitle,
                            testDate,
                            validUntil,
                            fullName,
                            birthDate
                        ),
                        infoClickListener = {
                            findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation(
                                toolbarTitle = getString(R.string.your_test_result_explanation_toolbar_title),
                                description =
                                getString(
                                    R.string.your_test_result_3_0_explanation_description,
                                    fullName,
                                    birthDate,
                                    testType,
                                    event.negativeTest?.name ?: "",
                                    testDate,
                                    resultDate,
                                    getString(R.string.your_test_result_explanation_negative_test_result),
                                    "",
                                    "",
                                    ""
                                )
                            ))
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

            val testDate = OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(result.sampleDate.toEpochSecond()),
                ZoneOffset.UTC
            ).formatDateTime(requireContext())

            val eventWidget = YourEventWidget(requireContext()).also {
                it.setContent(
                    title = getString(R.string.your_negative_test_results_row_title),
                    subtitle = getString(
                        R.string.your_negative_test_results_row_subtitle,
                        testDate,
                        result.sampleDate.plusHours(
                            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toLong()
                        ).formatDateTime(requireContext()),
                        "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
                    ),
                    infoClickListener = {
                        findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = getString(R.string.your_test_result_explanation_toolbar_title),
                            description =
                            getString(R.string.your_test_result_explanation_description,
                                "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}",
                                result.testType,
                                testDate,
                                getString(R.string.your_test_result_explanation_negative_test_result),
                                result.unique
                            )
                        ))
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
        remoteEvents: Map<RemoteEventsVaccinations, ByteArray>
    ) {
        binding.title.visibility = View.GONE
        binding.description.text =
            getString(R.string.your_retrieved_vaccinations_description)

        remoteEvents.keys.forEach { vaccinationEvents ->
            val fullName =
                "${vaccinationEvents.holder?.firstName} ${vaccinationEvents.holder?.infix} ${vaccinationEvents.holder?.lastName}"

            val birthDate = vaccinationEvents.holder?.birthDate?.let { birthDate ->
                try {
                    LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
                } catch (e: Exception) {
                    ""
                }
            } ?: ""

            vaccinationEvents.events?.let { events ->
                events
                    .sortedBy { it.vaccination?.date?.toEpochDay() }
                    .forEachIndexed { index, event ->

                        val doses = if (event.vaccination?.doseNumber != null && event.vaccination.totalDoses != null) {
                            getString(R.string.your_vaccination_explanation_doses, event.vaccination?.doseNumber, event.vaccination.totalDoses)
                        } else {
                            ""
                        }

                        val eventWidget = YourEventWidget(requireContext()).also {
                            it.setContent(
                                title = resources.getString(
                                    R.string.retrieved_vaccination_title,
                                    index + 1
                                ),
                                subtitle = resources.getString(
                                    R.string.your_vaccination_row_subtitle,
                                    fullName,
                                    birthDate
                                ),
                                infoClickListener = {
                                    findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation(
                                        toolbarTitle = getString(R.string.your_vaccination_explanation_toolbar_title),
                                        description =
                                        getString(
                                            R.string.your_vaccination_explanation_description,
                                            fullName,
                                            birthDate,
                                            getString(R.string.your_vaccination_explanation_covid_19),
                                            event.vaccination?.hpkCode ?: "",
                                            doses,
                                            event.vaccination?.date?.formatDayMonthYear() ?: "",
                                            event.vaccination?.country ?: "",
                                            event.unique ?: ""
                                        )
                                    ))
                                }
                            )
                        }
                        binding.eventsGroup.addView(eventWidget)
                    }
            }
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
