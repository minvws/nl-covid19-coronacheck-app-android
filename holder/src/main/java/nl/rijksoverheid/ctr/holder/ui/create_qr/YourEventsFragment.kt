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
import nl.rijksoverheid.ctr.design.ext.formatMonth
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.YourEventWidget
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.InfoScreenUtil
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
    private val infoScreenUtil: InfoScreenUtil by inject()
    private val dialogUtil: DialogUtil by inject()

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
            is YourEventsFragmentType.TestResult3 -> {
                presentTestResult3(
                    binding = binding,
                    remoteEvents = type.remoteEvents,
                )
            }
            is YourEventsFragmentType.TestResult2 -> {
                presentTestResult2(
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

        yourEventsViewModel.yourEventsResult.observe(viewLifecycleOwner, EventObserver { databaseSyncerResult ->
            when (databaseSyncerResult) {
                is DatabaseSyncerResult.Success -> {
                    // We have a origin in the database that we expect, so success
                    findNavController().navigate(
                        YourEventsFragmentDirections.actionMyOverview()
                    )
                }
                is DatabaseSyncerResult.MissingOrigin -> {
                    when (args.type) {
                        is YourEventsFragmentType.TestResult2, is YourEventsFragmentType.TestResult3 -> {
                            findNavController().navigate(
                                YourEventsFragmentDirections.actionCouldNotCreateQr(
                                    toolbarTitle = args.toolbarTitle,
                                    title = getString(R.string.rule_engine_no_origin_title),
                                    description = getString(R.string.rule_engine_no_test_origin_description)
                                )
                            )
                        }
                        is YourEventsFragmentType.Vaccination -> {
                            findNavController().navigate(
                                YourEventsFragmentDirections.actionCouldNotCreateQr(
                                    toolbarTitle = args.toolbarTitle,
                                    title = getString(R.string.rule_engine_no_origin_title),
                                    description = getString(R.string.rule_engine_no_vaccination_origin_description)
                                )
                            )
                        }
                    }
                }
                is DatabaseSyncerResult.NetworkError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_no_internet_connection_title,
                        message = getString(R.string.dialog_no_internet_connection_description),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
                is DatabaseSyncerResult.ServerError -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_error_title,
                        message = getString(
                            R.string.dialog_error_message_with_error_code,
                            databaseSyncerResult.httpCode.toString()
                        ),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
            }
        })
    }

    private fun presentTestResult3(
        binding: FragmentYourEventsBinding,
        remoteEvents: Map<RemoteTestResult3, ByteArray>
    ) {
        binding.title.setText(R.string.your_negative_test_results_title)
        binding.description.setHtmlText(getString(R.string.your_negative_test_results_description))

        remoteEvents.keys.forEach { negativeTests ->
            val fullName = getFullName(
                infix = negativeTests.holder?.infix,
                firstName = negativeTests.holder?.firstName,
                lastName = negativeTests.holder?.lastName
            )

            negativeTests.events?.forEach { event ->

                val testDate = event.negativeTest?.sampleDate?.let { sampleDate ->
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

                val infoScreen = infoScreenUtil.getForRemoteTestResult3(
                    event = event,
                    fullName = fullName,
                    testDate = testDate,
                    validUntil = validUntil,
                    birthDate = birthDate
                )

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
                                toolbarTitle = infoScreen.title,
                                description = infoScreen.description
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
                yourEventsViewModel.saveNegativeTests3(remoteEvents)
            }
        }

        binding.somethingWrongButton.setOnClickListener {
            findNavController().navigate(YourEventsFragmentDirections.actionShowSomethingWrong(
                description = getString(R.string.dialog_negative_test_result_something_wrong_description)
            ))
        }
    }

    private fun presentTestResult2(
        binding: FragmentYourEventsBinding,
        remoteTestResult: RemoteTestResult2,
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

            val infoScreen = infoScreenUtil.getForRemoteTestResult2(
                result = remoteTestResult.result,
                personalDetails = personalDetails,
                testDate = testDate
            )

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
                            toolbarTitle = infoScreen.title,
                            description = infoScreen.description
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
                yourEventsViewModel.saveNegativeTest2(
                    remoteTestResult = remoteTestResult,
                    rawResponse = remoteTestResultRawResponse
                )
            }
        }

        binding.somethingWrongButton.setOnClickListener {
            findNavController().navigate(YourEventsFragmentDirections.actionShowSomethingWrong(description = getString(R.string.dialog_negative_test_result_something_wrong_description)))
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
            val fullName = getFullName(
                infix = vaccinationEvents.holder?.infix,
                firstName = vaccinationEvents.holder?.firstName,
                lastName = vaccinationEvents.holder?.lastName
            )

            val birthDate = vaccinationEvents.holder?.birthDate?.let { birthDate ->
                try {
                    LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
                } catch (e: Exception) {
                    ""
                }
            } ?: ""

            vaccinationEvents.events?.let { events ->
                events
                    .sortedByDescending {
                        it.vaccination?.date?.toEpochDay()
                    }
                    .forEachIndexed { index, event ->

                        val infoScreen = infoScreenUtil.getForRemoteVaccination(
                            event = event,
                            fullName = fullName,
                            birthDate = birthDate
                        )

                        val eventWidget = YourEventWidget(requireContext()).also {
                            it.setContent(
                                title = resources.getString(
                                    R.string.retrieved_vaccination_title,
                                    event.vaccination?.date?.formatMonth(),
                                    cachedAppConfigUseCase.getProviderName(vaccinationEvents.providerIdentifier)
                                ),
                                subtitle = resources.getString(
                                    R.string.your_vaccination_row_subtitle,
                                    fullName,
                                    birthDate
                                ),
                                infoClickListener = {
                                    findNavController().navigate(YourEventsFragmentDirections.actionShowExplanation(
                                        toolbarTitle = infoScreen.title,
                                        description = infoScreen.description
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
            yourEventsViewModel.saveVaccinations(
                remoteEvents = remoteEvents
            )
        }

        binding.somethingWrongButton.setOnClickListener {
            findNavController().navigate(YourEventsFragmentDirections.actionShowSomethingWrong(description = getString(R.string.dialog_vaccination_something_wrong_description)))
        }
    }

    private fun getFullName(infix: String?, firstName: String?, lastName: String?): String {
        return if (infix.isNullOrEmpty()) "${lastName}, $firstName" else "$infix ${lastName}, $firstName"
    }
}
