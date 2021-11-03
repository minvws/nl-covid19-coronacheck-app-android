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
import androidx.core.view.forEachIndexed
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatMonth
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.design.utils.BottomSheetDialogUtil
import nl.rijksoverheid.ctr.design.utils.DescriptionData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.items.YourEventWidget
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.RemoteProtocol3Util
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class YourEventsFragment : BaseFragment(R.layout.fragment_your_events) {

    private val args: YourEventsFragmentArgs by navArgs()

    private val personalDetailsUtil: PersonalDetailsUtil by inject()
    private val infoScreenUtil: InfoScreenUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    private val bottomSheetDialogUtil: BottomSheetDialogUtil by inject()

    private val remoteProtocol3Util: RemoteProtocol3Util by inject()
    private val remoteEventUtil: RemoteEventUtil by inject()

    private val yourEventsViewModel: YourEventsViewModel by viewModel()

    override fun onButtonClickWithRetryTitle(): Int {
        return R.string.dialog_retry
    }

    override fun onButtonClickWithRetryAction() {
        navigateSafety(YourEventsFragmentDirections.actionMyOverview())
    }

    private fun retrieveGreenCards() {
        when (val type = args.type) {
            is YourEventsFragmentType.TestResult2 -> {
                yourEventsViewModel.saveNegativeTest2(
                    negativeTest2 = type.remoteTestResult,
                    rawResponse = type.rawResponse
                )
            }
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                yourEventsViewModel.checkForConflictingEvents(
                    remoteProtocols3 = type.remoteEvents,
                )
            }
            is YourEventsFragmentType.DCC -> {
                yourEventsViewModel.checkForConflictingEvents(
                    remoteProtocols3 = type.remoteEvents,
                )
            }
        }
    }

    override fun getFlow(): Flow {
        when (val type = args.type) {
            is YourEventsFragmentType.TestResult2 -> {
                return HolderFlow.CommercialTest
            }
            is YourEventsFragmentType.DCC -> {
                return HolderFlow.HkviScan
            }
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                return when (type.originType) {
                    is OriginType.Test -> {
                        if (type.fromCommercialTestCode) {
                            HolderFlow.CommercialTest
                        } else {
                            HolderFlow.DigidTest
                        }
                    }
                    is OriginType.Recovery -> {
                        HolderFlow.Recovery
                    }
                    is OriginType.Vaccination -> {
                        HolderFlow.Vaccination
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentYourEventsBinding.bind(view)

        presentHeader(
            binding = binding
        )

        presentEvents(
            binding = binding
        )

        presentFooter(
            binding = binding
        )

        handleButton(
            binding = binding
        )

        blockBackButton()

        yourEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
            binding.bottom.setButtonEnabled(!it)
            binding.eventsGroup.forEachIndexed { index, _ ->
                val eventGroup = binding.eventsGroup.getChildAt(index) as YourEventWidget
                eventGroup.setButtonsEnabled(!it)
            }
        })

        yourEventsViewModel.yourEventsResult.observe(
            viewLifecycleOwner,
            EventObserver { databaseSyncerResult ->
                when (databaseSyncerResult) {
                    is DatabaseSyncerResult.Success -> {
                        if (databaseSyncerResult.missingOrigin) {
                            navigateSafety(
                                YourEventsFragmentDirections.actionCouldNotCreateQr(
                                    toolbarTitle = args.toolbarTitle,
                                    title = getString(R.string.rule_engine_no_origin_title),
                                    description = getString(R.string.rule_engine_no_test_origin_description, args.toolbarTitle.lowercase()),
                                    buttonTitle = getString(R.string.back_to_overview)
                                )
                            )
                        } else {
                            // We have a origin in the database that we expect, so success
                            navigateSafety(
                                YourEventsFragmentDirections.actionMyOverview()
                            )
                        }
                    }
                    is DatabaseSyncerResult.Failed -> {
                        presentError(
                            errorResult = databaseSyncerResult.errorResult
                        )
                    }
                }
            })

        yourEventsViewModel.conflictingEventsResult.observe(
            viewLifecycleOwner,
            EventObserver {
                when (val type = args.type) {
                    is YourEventsFragmentType.RemoteProtocol3Type -> {
                        if (it) {
                            replaceCertificateDialog(type.remoteEvents, type.originType)
                        } else {
                            yourEventsViewModel.saveRemoteProtocol3Events(
                                type.remoteEvents,
                                type.originType,
                                false
                            )
                        }
                    }
                    is YourEventsFragmentType.DCC -> {
                        if (it) {
                            replaceCertificateDialog(type.remoteEvents, type.originType)
                        } else {
                            yourEventsViewModel.saveRemoteProtocol3Events(
                                type.remoteEvents,
                                type.originType,
                                false
                            )
                        }
                    }
                }
            }
        )
    }

    private fun replaceCertificateDialog(
        remoteEvents: Map<RemoteProtocol3, ByteArray>,
        originType: OriginType
    ) {
        dialogUtil.presentDialog(
            context = requireContext(),
            title = R.string.your_events_replace_dialog_title,
            message = getString(R.string.your_events_replace_dialog_message),
            positiveButtonText = R.string.your_events_replace_dialog_positive_button,
            positiveButtonCallback = {
                yourEventsViewModel.saveRemoteProtocol3Events(
                    remoteProtocols3 = remoteEvents,
                    originType = originType,
                    removePreviousEvents = true
                )
            },
            negativeButtonText = R.string.your_events_replace_dialog_negative_button,
            negativeButtonCallback = {
                navigateSafety(
                    YourEventsFragmentDirections.actionMyOverview()
                )
            }
        )
    }

    private fun presentHeader(binding: FragmentYourEventsBinding) {
        when (val type = args.type) {
            is YourEventsFragmentType.TestResult2 -> {
                binding.title.setText(R.string.your_negative_test_results_title)
                binding.description.setHtmlText(R.string.your_negative_test_results_description)
            }
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                when (type.originType) {
                    is OriginType.Test -> {
                        binding.title.setText(R.string.your_negative_test_results_title)
                        binding.description.setHtmlText(R.string.your_negative_test_results_description)
                    }
                    is OriginType.Vaccination -> {
                        binding.title.visibility = View.GONE
                        binding.description.setHtmlText(R.string.your_retrieved_vaccinations_description)
                    }
                    is OriginType.Recovery -> {
                        binding.title.visibility = View.GONE
                        binding.description.setHtmlText(R.string.your_positive_test_description)
                    }
                }
            }
            is YourEventsFragmentType.DCC -> {
                binding.title.visibility = View.GONE
                binding.description.setHtmlText(R.string.your_dcc_event_description)
            }
        }
    }

    private fun presentEvents(binding: FragmentYourEventsBinding) {
        when (val type = args.type) {
            is YourEventsFragmentType.TestResult2 -> {
                presentTestResult2(
                    binding = binding,
                    remoteProtocol2 = type.remoteTestResult
                )
            }
            is YourEventsFragmentType.RemoteProtocol3Type -> presentEvents(
                type.remoteEvents,
                binding
            )
            is YourEventsFragmentType.DCC -> presentEvents(
                type.remoteEvents,
                binding,
                isDccEvent = true
            )
        }
    }

    private fun getProviderName(providerIdentifier: String): String {
        return (args.type as? YourEventsFragmentType.RemoteProtocol3Type)
            ?.eventProviders?.firstOrNull { it.identifier == providerIdentifier }
            ?.name
            ?: providerIdentifier
    }

    private fun presentEvents(
        remoteEvents: Map<RemoteProtocol3, ByteArray>,
        binding: FragmentYourEventsBinding,
        isDccEvent: Boolean = false
    ) {
        val protocols = remoteEvents.map { it.key }

        val groupedEvents = remoteProtocol3Util.groupEvents(protocols)

        groupedEvents.forEach { protocolGroupedEvent ->
            val holder = protocolGroupedEvent.value.firstOrNull()?.holder
            val providerIdentifiers = protocolGroupedEvent.value.map { it.providerIdentifier }.map { getProviderName(it) }
            val allSameEvents = protocolGroupedEvent.value.map { it.remoteEvent }
            val allEventsInformation = protocolGroupedEvent.value.map { RemoteEventInformation(it.providerIdentifier, holder, it.remoteEvent) }
            remoteEventUtil.removeDuplicateEvents(allSameEvents).forEach { remoteEvent ->
                when (remoteEvent) {
                    is RemoteEventVaccination -> {
                        presentVaccinationEvent(
                            binding = binding,
                            providerIdentifiers = providerIdentifiers.toSet()
                                .joinToString(" ${getString(R.string.your_events_and)} "),
                            fullName = getFullName(holder),
                            birthDate = getBirthDate(holder),
                            currentEvent = remoteEvent,
                            allEventsInformation = allEventsInformation,
                            isDccEvent = isDccEvent
                        )
                    }
                    is RemoteEventNegativeTest -> {
                        presentNegativeTestEvent(
                            binding = binding,
                            fullName = getFullName(holder),
                            birthDate = getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                    is RemoteEventPositiveTest -> {
                        presentPositiveTestEvent(
                            binding = binding,
                            fullName = getFullName(holder),
                            birthDate = getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                    is RemoteEventRecovery -> {
                        presentRecoveryEvent(
                            binding = binding,
                            fullName = getFullName(holder),
                            birthDate = getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                }
            }
        }
    }

    private fun presentTestResult2(
        binding: FragmentYourEventsBinding,
        remoteProtocol2: RemoteTestResult2
    ) {
        remoteProtocol2.result?.let { result ->
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
                result = remoteProtocol2.result,
                personalDetails = personalDetails,
                testDate = testDate
            )

            val eventWidget = YourEventWidget(requireContext()).apply {
                setContent(
                    title = getString(R.string.your_negative_test_results_row_title),
                    subtitle = getString(
                        R.string.your_negative_test_results_row_subtitle,
                        testDate,
                        "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
                    ),
                    infoClickListener = {
                        navigateSafety(
                            YourEventsFragmentDirections.actionShowExplanation(
                                data = arrayOf(infoScreen),
                                toolbarTitle = infoScreen.title
                            )
                        )
                    }
                )
            }
            binding.eventsGroup.addView(eventWidget)
        }
    }

    private fun presentVaccinationEvent(
        binding: FragmentYourEventsBinding,
        providerIdentifiers: String,
        fullName: String,
        birthDate: String,
        currentEvent: RemoteEventVaccination,
        allEventsInformation: List<RemoteEventInformation>,
        isDccEvent: Boolean,
    ) {

        val infoScreen = infoScreenUtil.getForVaccination(
            event = currentEvent,
            fullName = fullName,
            birthDate = birthDate,
            providerIdentifier = allEventsInformation.first().providerIdentifier
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = getVaccinationEventTitle(isDccEvent, currentEvent),
                subtitle = resources.getString(
                    R.string.your_vaccination_row_subtitle,
                    fullName,
                    birthDate,
                    providerIdentifiers,
                ),
                infoClickListener = {
                    navigateSafety(
                        YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = infoScreen.title,
                            data = allEventsInformation.map {
                                val vaccinationEvent = it.remoteEvent as RemoteEventVaccination
                                infoScreenUtil.getForVaccination(
                                    event = vaccinationEvent,
                                    fullName = fullName,
                                    birthDate = birthDate,
                                    providerIdentifier = getProviderName(it.providerIdentifier),
                                )
                            }.toTypedArray()
                        )
                    )
                }
            )
        }
        binding.eventsGroup.addView(eventWidget)
    }

    private fun YourEventWidget.getVaccinationEventTitle(
        isDccEvent: Boolean,
        currentEvent: RemoteEventVaccination
    ) = if (isDccEvent) {
        resources.getString(R.string.retrieved_vaccination_dcc_title)
    } else {
        resources.getString(
            R.string.retrieved_vaccination_title,
            currentEvent.vaccination?.date?.formatMonth(),
        )
    }

    private fun presentNegativeTestEvent(
        binding: FragmentYourEventsBinding,
        fullName: String,
        birthDate: String,
        event: RemoteEventNegativeTest
    ) {
        val testDate = event.negativeTest?.sampleDate?.formatDateTime(requireContext()) ?: ""

        val infoScreen = infoScreenUtil.getForNegativeTest(
            event = event,
            fullName = fullName,
            testDate = testDate,
            birthDate = birthDate
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = getString(R.string.your_negative_test_results_row_title),
                subtitle = getString(
                    R.string.your_negative_test_3_0_results_row_subtitle,
                    testDate,
                    fullName,
                    birthDate
                ),
                infoClickListener = {
                    navigateSafety(
                        YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = infoScreen.title,
                            data = arrayOf(infoScreen)
                        )
                    )
                }
            )
        }
        binding.eventsGroup.addView(eventWidget)
    }

    private fun presentPositiveTestEvent(
        binding: FragmentYourEventsBinding,
        fullName: String,
        birthDate: String,
        event: RemoteEventPositiveTest
    ) {
        val testDate = event.positiveTest?.sampleDate?.formatDateTime(requireContext()) ?: ""

        val infoScreen = infoScreenUtil.getForPositiveTest(
            event = event,
            testDate = testDate,
            fullName = fullName,
            birthDate = birthDate
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = getString(R.string.positive_test_title),
                subtitle = getString(
                    R.string.your_negative_test_3_0_results_row_subtitle,
                    testDate,
                    fullName,
                    birthDate
                ),
                infoClickListener = {
                    navigateSafety(
                        YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = infoScreen.title,
                            data = arrayOf(infoScreen)
                        )
                    )
                }
            )
        }
        binding.eventsGroup.addView(eventWidget)
    }

    private fun presentRecoveryEvent(
        binding: FragmentYourEventsBinding,
        fullName: String,
        birthDate: String,
        event: RemoteEventRecovery
    ) {
        val testDate = event.recovery?.sampleDate?.formatDayMonthYear() ?: ""

        val infoScreen = infoScreenUtil.getForRecovery(
            event = event,
            fullName = fullName,
            testDate = testDate,
            birthDate = birthDate
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = getString(R.string.positive_test_title),
                subtitle = getString(
                    R.string.your_negative_test_3_0_results_row_subtitle,
                    testDate,
                    fullName,
                    birthDate
                ),
                infoClickListener = {
                    navigateSafety(
                        YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = infoScreen.title,
                            data = arrayOf(infoScreen)
                        )
                    )
                }
            )
        }
        binding.eventsGroup.addView(eventWidget)
    }

    private fun handleButton(binding: FragmentYourEventsBinding) {
        binding.bottom.setButtonClick {
            retrieveGreenCards()
        }
    }

    private fun presentFooter(binding: FragmentYourEventsBinding) {
        binding.somethingWrongButton.setOnClickListener {
            val type = args.type
            bottomSheetDialogUtil.present(childFragmentManager, BottomSheetData.TitleDescription(
                title = getString(R.string.dialog_negative_test_result_something_wrong_title),
                descriptionData = DescriptionData(
                    htmlText = if (type is YourEventsFragmentType.RemoteProtocol3Type && type.originType is OriginType.Vaccination) {
                        R.string.dialog_vaccination_something_wrong_description
                    } else {
                        R.string.dialog_negative_test_result_something_wrong_description
                    },
                    htmlLinksEnabled = true,
                )
            ))
        }
    }

    private fun blockBackButton() {
        // Catch back button to show modal instead
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.your_events_block_back_dialog_title)
                        .setMessage(R.string.your_events_block_back_dialog_description)
                        .setPositiveButton(R.string.your_events_block_back_dialog_positive_button) { _, _ ->
                            navigateSafety(
                                YourEventsFragmentDirections.actionMyOverview()
                            )
                        }
                        .setNegativeButton(R.string.your_events_block_back_dialog_negative_button) { _, _ -> }
                        .show()
                }
            }
        })
    }

    private fun getFullName(holder: RemoteProtocol3.Holder?): String = holder?.let {
        return if (it.infix.isNullOrEmpty()) {
            "${it.lastName}, ${it.firstName}"
        } else {
            "${it.infix} ${it.lastName}, ${it.firstName}"
        }
    } ?: ""

    private fun getBirthDate(holder: RemoteProtocol3.Holder?): String =
        holder?.birthDate?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: DateTimeParseException) {
                // Check if date has removed content, if so return string directly
                if (birthDate.contains("XX")) {
                    birthDate
                } else ""
            } catch (e: Exception) {
                ""
            }
        } ?: ""

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }
}