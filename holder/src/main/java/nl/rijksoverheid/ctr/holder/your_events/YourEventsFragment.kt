/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.forEachIndexed
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonth
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTime
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.MissingOriginErrorResult
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3Util
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidget
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtil
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.Flow
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class YourEventsFragment : BaseFragment(R.layout.fragment_your_events) {

    private val args: YourEventsFragmentArgs by navArgs()

    private val infoScreenUtil: InfoScreenUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    private val remoteProtocol3Util: RemoteProtocol3Util by inject()
    private val remoteEventUtil: RemoteEventUtil by inject()
    private val yourEventsFragmentUtil: YourEventsFragmentUtil by inject()
    private val yourEventWidgetUtil: YourEventWidgetUtil by inject()
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase by inject()

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    private val yourEventsViewModel: YourEventsViewModel by viewModel()

    override fun onButtonClickWithRetryTitle(): Int {
        return R.string.dialog_retry
    }

    override fun onButtonClickWithRetryAction() {
        navigateSafety(YourEventsFragmentDirections.actionMyOverview())
    }

    private fun retrieveGreenCards() {
        when (val type = args.type) {
            is YourEventsFragmentType.RemoteProtocol3Type -> {
                yourEventsViewModel.checkForConflictingEvents(
                    remoteProtocols = type.remoteEvents
                )
            }
            is YourEventsFragmentType.DCC -> {
                yourEventsViewModel.checkForConflictingEvents(
                    remoteProtocols = type.getRemoteEvents()
                )
            }
        }
    }

    override fun getFlow(): Flow {
        return args.flow
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
                        when {
                            databaseSyncerResult.missingOrigin -> {
                                val noOriginTypeCopy =
                                    getString(yourEventsFragmentUtil.getNoOriginTypeCopy(args.type, getFlow()))
                                presentError(
                                    errorResult = MissingOriginErrorResult,
                                    customerErrorDescription = getString(
                                        R.string.rule_engine_no_test_origin_description,
                                        noOriginTypeCopy
                                    )
                                )
                            }
                            else -> {
                                if (args.type is YourEventsFragmentType.DCC) {
                                    navigateSafety(YourEventsFragmentDirections.actionMyOverview())
                                } else {
                                    navigateToCertificateCreated(databaseSyncerResult)
                                }
                            }
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
                            replaceCertificateDialog(type.remoteEvents)
                        } else {
                            yourEventsViewModel.saveRemoteProtocolEvents(
                                getFlow(), type.remoteEvents, false
                            )
                        }
                    }
                    is YourEventsFragmentType.DCC -> {
                        if (it) {
                            replaceCertificateDialog(type.getRemoteEvents())
                        } else {
                            yourEventsViewModel.saveRemoteProtocolEvents(
                                getFlow(), type.getRemoteEvents(), false
                            )
                        }
                    }
                }
            }
        )
    }

    private fun navigateToCertificateCreated(databaseSyncerResult: DatabaseSyncerResult.Success) {
        when (databaseSyncerResult.yourEventFragmentEndState) {
            is YourEventFragmentEndState.CombinedVaccinationRecovery -> {
                navigateSafety(
                    YourEventsFragmentDirections.actionCertificateCreated(
                        toolbarTitle = getString(R.string.international_certificate_created_toolbar_title),
                        title = if (isVaccinationWithPositiveTestFlow()) {
                            getString(R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_title)
                        } else {
                            getString(R.string.certificate_created_recovery_after_vaccination_title)
                        },
                        description = if (isVaccinationWithPositiveTestFlow()) {
                            getString(
                                R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_message
                            )
                        } else {
                            getString(R.string.certificate_created_recovery_after_vaccination_description)
                        }
                    )
                )
            }
            YourEventFragmentEndState.OnlyInternationalVaccination -> {
                navigateSafety(
                    YourEventsFragmentDirections.actionCertificateCreated(
                        toolbarTitle = getString(R.string.international_certificate_created_toolbar_title),
                        title = getString(R.string.holder_listRemoteEvents_endStateInternationalQROnly_title),
                        description = getString(R.string.holder_listRemoteEvents_endStateInternationalQROnly_message),
                        shouldShowRetrieveTestButton = true
                    )
                )
            }
            YourEventFragmentEndState.VaccinationAndRecovery -> {
                navigateSafety(
                    if (isVaccinationWithPositiveTestFlow()) {
                        YourEventsFragmentDirections.actionCertificateCreated(
                            toolbarTitle = getString(R.string.international_certificate_created_toolbar_title),
                            title = getString(R.string.holder_listRemoteEvents_endStateInternationalVaccinationAndRecovery_title),
                            description = if (holderFeatureFlagUseCase.getDisclosurePolicy() != DisclosurePolicy.ZeroG) {
                                getString(R.string.holder_listRemoteEvents_endStateInternationalVaccinationAndRecovery_message)
                            } else {
                                getString(R.string.holder_listRemoteEvents_endStateVaccinationsAndRecovery_message)
                            }
                        )
                    } else {
                        YourEventsFragmentDirections.actionMyOverview()
                    }
                )
            }
            YourEventFragmentEndState.OnlyRecovery -> {
                navigateSafety(
                    if (isVaccinationWithPositiveTestFlow()) {
                        YourEventsFragmentDirections.actionCertificateCreated(
                            toolbarTitle = getString(R.string.international_certificate_created_toolbar_title),
                            title = getString(R.string.holder_listRemoteEvents_endStateRecoveryOnly_title),
                            description = getString(R.string.holder_listRemoteEvents_endStateRecoveryOnly_message)
                        )
                    } else {
                        // When not coming from a vaccination completion flow, navigate directly to dashboard
                        YourEventsFragmentDirections.actionMyOverview()
                    }
                )
            }
            is YourEventFragmentEndState.OnlyDomesticVaccination -> {
                navigateSafety(
                    if (isVaccinationWithPositiveTestFlow()) {
                        // When coming from a vaccination completion flow, navigate directly to dashboard
                        YourEventsFragmentDirections.actionMyOverview()
                    } else {
                        YourEventsFragmentDirections.actionCertificateCreated(
                            toolbarTitle = getString(R.string.international_certificate_created_toolbar_title),
                            title = getString(R.string.certificate_created_vaccination_title),
                            description = getString(
                                R.string.certificate_created_vaccination_description
                            )
                        )
                    }
                )
            }
            YourEventFragmentEndState.NotApplicable -> {
                // We have a origin in the database that we expect, so success
                navigateSafety(
                    YourEventsFragmentDirections.actionMyOverview()
                )
            }
            YourEventFragmentEndState.AddedNegativeTestInVaccinationAssessmentFlow -> {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    infoFragmentDirections = YourEventsFragmentDirections.actionInfoFragment(
                        toolbarTitle = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_toolbar),
                        data = InfoFragmentData.TitleDescriptionWithButton(
                            title = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_title),
                            descriptionData = DescriptionData(
                                htmlText = R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_body
                            ),
                            primaryButtonData = ButtonData.NavigationButton(
                                text = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_button_complete),
                                navigationActionId = InfoFragmentDirections.actionVisitorPassInputToken().actionId
                            )
                        )
                    )
                )
            }
            YourEventFragmentEndState.NoRecoveryWithStoredVaccination -> {
                navigateSafety(
                    YourEventsFragmentDirections.actionCertificateCreated(
                        toolbarTitle = getString(R.string.no_certificate_created_toolbar_title),
                        title = getString(R.string.cannot_create_recovery_proof_title),
                        description = getString(R.string.cannot_create_recovery_proof_description)
                    )
                )
            }
        }
    }

    private fun isVaccinationWithPositiveTestFlow() =
        getFlow() == HolderFlow.VaccinationAndPositiveTest

    private fun replaceCertificateDialog(
        remoteEvents: Map<RemoteProtocol, ByteArray>
    ) {
        dialogUtil.presentDialog(
            context = requireContext(),
            title = R.string.your_events_replace_dialog_title,
            message = getString(R.string.your_events_replace_dialog_message),
            positiveButtonText = R.string.your_events_replace_dialog_positive_button,
            positiveButtonCallback = {
                yourEventsViewModel.saveRemoteProtocolEvents(
                    flow = getFlow(),
                    remoteProtocols = remoteEvents,
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

    private fun presentEvents(binding: FragmentYourEventsBinding) {
        when (val type = args.type) {
            is YourEventsFragmentType.RemoteProtocol3Type -> presentEvents(
                type.remoteEvents,
                binding
            )
            is YourEventsFragmentType.DCC -> presentEvents(
                type.getRemoteEvents(),
                binding,
                isDccEvent = true
            )
        }
    }

    private fun presentEvents(
        remoteEvents: Map<RemoteProtocol, ByteArray>,
        binding: FragmentYourEventsBinding,
        isDccEvent: Boolean = false
    ) {
        val protocols = remoteEvents.map { it.key }

        val groupedEvents = remoteProtocol3Util.groupEvents(protocols)

        groupedEvents.forEach { protocolGroupedEvent ->
            val holder = protocolGroupedEvent.value.firstOrNull()?.holder
            val providerIdentifiers =
                protocolGroupedEvent.value.map { it.providerIdentifier }
                    .map {
                        yourEventsFragmentUtil.getProviderName(
                            providers = cachedAppConfigUseCase.getCachedAppConfig().providers,
                            providerIdentifier = it
                        )
                    }

            val allSameEvents = protocolGroupedEvent.value.map { it.remoteEvent }
            val allEventsInformation = protocolGroupedEvent.value.map {
                RemoteEventInformation(it.providerIdentifier, holder, it.remoteEvent)
            }
            remoteEventUtil.removeDuplicateEvents(allSameEvents).forEach { remoteEvent ->
                when (remoteEvent) {
                    is RemoteEventVaccination -> {
                        presentVaccinationEvent(
                            binding = binding,
                            providerIdentifiers = providerIdentifiers.toSet()
                                .joinToString(" ${getString(R.string.your_events_and)} "),
                            vaccinationDate = yourEventsFragmentUtil.getVaccinationDate(remoteEvent.vaccination?.date),
                            fullName = yourEventsFragmentUtil.getFullName(holder),
                            birthDate = yourEventsFragmentUtil.getBirthDate(holder),
                            currentEvent = remoteEvent,
                            allEventsInformation = allEventsInformation,
                            isDccEvent = isDccEvent
                        )
                    }
                    is RemoteEventNegativeTest -> {
                        presentNegativeTestEvent(
                            binding = binding,
                            fullName = yourEventsFragmentUtil.getFullName(holder),
                            birthDate = yourEventsFragmentUtil.getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                    is RemoteEventPositiveTest -> {
                        presentPositiveTestEvent(
                            binding = binding,
                            fullName = yourEventsFragmentUtil.getFullName(holder),
                            birthDate = yourEventsFragmentUtil.getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                    is RemoteEventRecovery -> {
                        presentRecoveryEvent(
                            binding = binding,
                            fullName = yourEventsFragmentUtil.getFullName(holder),
                            birthDate = yourEventsFragmentUtil.getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                    is RemoteEventVaccinationAssessment -> {
                        presentVaccinationAssessmentEvent(
                            binding = binding,
                            fullName = yourEventsFragmentUtil.getFullName(holder),
                            birthDate = yourEventsFragmentUtil.getBirthDate(holder),
                            event = remoteEvent
                        )
                    }
                }
            }
        }
    }

    private fun presentVaccinationEvent(
        binding: FragmentYourEventsBinding,
        providerIdentifiers: String,
        vaccinationDate: String,
        fullName: String,
        birthDate: String,
        currentEvent: RemoteEventVaccination,
        allEventsInformation: List<RemoteEventInformation>,
        isDccEvent: Boolean
    ) {
        val type = args.type
        val infoScreen = infoScreenUtil.getForVaccination(
            event = currentEvent,
            fullName = fullName,
            birthDate = birthDate,
            providerIdentifier = allEventsInformation.first().providerIdentifier,
            europeanCredential = if (type is YourEventsFragmentType.DCC) {
                JSONObject(type.eventGroupJsonData.decodeToString()).getString("credential").toByteArray()
            } else {
                null
            }
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = yourEventWidgetUtil.getVaccinationEventTitle(
                    context,
                    isDccEvent,
                    currentEvent
                ),
                subtitle = yourEventWidgetUtil.getVaccinationEventSubtitle(
                    context,
                    isDccEvent,
                    providerIdentifiers,
                    vaccinationDate,
                    fullName,
                    birthDate
                ),
                infoClickListener = {
                    navigateSafety(
                        YourEventsFragmentDirections.actionShowExplanation(
                            toolbarTitle = infoScreen.title,
                            data = allEventsInformation.map {
                                val vaccinationEvent =
                                    it.remoteEvent as RemoteEventVaccination
                                infoScreenUtil.getForVaccination(
                                    event = vaccinationEvent,
                                    fullName = fullName,
                                    birthDate = birthDate,
                                    providerIdentifier = yourEventsFragmentUtil.getProviderName(
                                        providers = cachedAppConfigUseCase.getCachedAppConfig().providers,
                                        providerIdentifier = it.providerIdentifier
                                    ),
                                    europeanCredential = if (type is YourEventsFragmentType.DCC) {
                                        JSONObject(type.eventGroupJsonData.decodeToString()).getString("credential").toByteArray()
                                    } else {
                                        null
                                    }
                                )
                            }.toTypedArray()
                        )
                    )
                }
            )
        }
        binding.eventsGroup.addView(eventWidget)
    }

    private fun presentNegativeTestEvent(
        binding: FragmentYourEventsBinding,
        fullName: String,
        birthDate: String,
        event: RemoteEventNegativeTest
    ) {
        val type = args.type

        val testDate =
            event.negativeTest?.sampleDate?.formatDateTime(requireContext()) ?: ""

        val infoScreen = infoScreenUtil.getForNegativeTest(
            event = event,
            fullName = fullName,
            testDate = testDate,
            birthDate = birthDate,
            europeanCredential = if (type is YourEventsFragmentType.DCC) {
                JSONObject(type.eventGroupJsonData.decodeToString()).getString("credential").toByteArray()
            } else {
                null
            }
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

    private fun presentVaccinationAssessmentEvent(
        binding: FragmentYourEventsBinding,
        fullName: String,
        birthDate: String,
        event: RemoteEventVaccinationAssessment
    ) {
        val assessmentDate =
            event.vaccinationAssessment.assessmentDate?.toLocalDate()?.formatDayMonth()

        val infoScreen = infoScreenUtil.getForVaccinationAssessment(
            event = event,
            fullName = fullName,
            birthDate = birthDate
        )

        val eventWidget = YourEventWidget(requireContext()).apply {
            setContent(
                title = getString(R.string.holder_event_vaccination_assessment_element_title),
                subtitle = getString(
                    R.string.holder_event_vaccination_assessment_element_subtitle,
                    assessmentDate,
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
        val testDate =
            event.positiveTest?.sampleDate?.formatDayMonthYearTime(requireContext()) ?: ""

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
        val type = args.type
        val testDate = event.recovery?.sampleDate?.formatDayMonthYear() ?: ""

        val infoScreen = infoScreenUtil.getForRecovery(
            event = event,
            fullName = fullName,
            testDate = testDate,
            birthDate = birthDate,
            europeanCredential = if (type is YourEventsFragmentType.DCC) {
                JSONObject(type.eventGroupJsonData.decodeToString()).getString("credential").toByteArray()
            } else {
                null
            }
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

    private fun presentHeader(binding: FragmentYourEventsBinding) {
        binding.description.setText(yourEventsFragmentUtil.getHeaderCopy(args.type))
    }

    private fun presentFooter(binding: FragmentYourEventsBinding) {
        binding.somethingWrongButton.run {
            visibility = if (args.type is YourEventsFragmentType.DCC) View.GONE else View.VISIBLE
            setOnClickListener {
                val type = args.type
                infoFragmentUtil.presentAsBottomSheet(
                    childFragmentManager, InfoFragmentData.TitleDescription(
                        title = getString(R.string.holder_listRemoteEvents_somethingWrong_title),
                        descriptionData = DescriptionData(
                            htmlText = if (type is YourEventsFragmentType.RemoteProtocol3Type) {
                                val origins = type.remoteEvents.keys
                                    .flatMap { it.events ?: emptyList() }
                                    .map { remoteEventUtil.getOriginType(it) }
                                when {
                                    origins.all { it == OriginType.Vaccination } -> {
                                        if (getFlow() == HolderFlow.VaccinationAndPositiveTest) {
                                            R.string.holder_listRemoteEvents_somethingWrong_vaccinationAndPositiveTest_body
                                        } else {
                                            R.string.holder_listRemoteEvents_somethingWrong_vaccination_body
                                        }
                                    }
                                    origins.all { it == OriginType.VaccinationAssessment } -> {
                                        R.string.holder_event_vaccination_assessment_wrong_body
                                    }
                                    origins.all { it == OriginType.Recovery } -> {
                                        R.string.dialog_negative_test_result_something_wrong_description
                                    }
                                    origins.contains(OriginType.Vaccination) &&
                                            origins.contains(OriginType.Recovery) -> {
                                        R.string.holder_listRemoteEvents_somethingWrong_vaccinationAndPositiveTest_body
                                    }
                                    else -> R.string.dialog_negative_test_result_something_wrong_description
                                }
                            } else {
                                R.string.dialog_negative_test_result_something_wrong_description
                            },
                            htmlLinksEnabled = true
                        )
                    )
                )
            }
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
                        .setMessage(
                            yourEventsFragmentUtil.getCancelDialogDescription(
                                type = args.type
                            )
                        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as? HolderMainFragment)?.presentLoading(false)
    }
}
