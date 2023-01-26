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
import nl.rijksoverheid.ctr.design.ext.formatDayMonth
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYearTime
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourEventsBinding
import nl.rijksoverheid.ctr.holder.fuzzy_matching.MatchingBlobIds
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.your_events.models.ConflictingEventResult
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndState
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndStateWithCustomTitle
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3Util
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsEndStateUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidget
import nl.rijksoverheid.ctr.holder.your_events.widgets.YourEventWidgetUtil
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.BlockedEventException
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
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
    private val yourEventsEndStateUtil: YourEventsEndStateUtil by inject()

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    private val yourEventsViewModel: YourEventsViewModel by viewModel()

    override fun onButtonClickWithRetryTitle(): Int {
        return R.string.dialog_retry
    }

    override fun onButtonClickWithRetryAction() {
        findNavControllerSafety()?.popBackStack()
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
                val fragmentType = args.type

                when (databaseSyncerResult) {
                    is DatabaseSyncerResult.Success -> {
                        handleEndState(
                            endState = yourEventsEndStateUtil.getEndState(
                                context = requireContext(),
                                hints = databaseSyncerResult.hints,
                                blockedEvents = databaseSyncerResult.blockedEvents,
                                newEvents = when (fragmentType) {
                                    is YourEventsFragmentType.DCC -> fragmentType.remoteEvent.events ?: listOf()
                                    is YourEventsFragmentType.RemoteProtocol3Type -> fragmentType.remoteEvents.keys.toList().map { it.events ?: listOf() }.flatten()
                                }
                            )
                        )
                    }
                    is DatabaseSyncerResult.Failed -> {
                        presentError(
                            errorResult = databaseSyncerResult.errorResult
                        )
                    }
                    is DatabaseSyncerResult.FuzzyMatchingError -> {
                        navigateSafety(YourEventsFragmentDirections.actionFuzzyMatching(
                            MatchingBlobIds(databaseSyncerResult.matchingBlobIds)
                        ))
                    }
                }
            })

        yourEventsViewModel.conflictingEventsResult.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    ConflictingEventResult.Existing -> {
                        infoFragmentUtil.presentFullScreen(
                            currentFragment = this,
                            toolbarTitle = args.toolbarTitle,
                            data = InfoFragmentData.TitleDescriptionWithButton(
                                title = getString(R.string.holder_listRemoteEvents_endStateDuplicate_title),
                                descriptionData = DescriptionData(
                                    htmlText = R.string.holder_listRemoteEvents_endStateDuplicate_message
                                ),
                                primaryButtonData = ButtonData.NavigationButton(
                                    text = getString(R.string.general_toMyOverview),
                                    navigationActionId = R.id.action_my_overview
                                )
                            ),
                            hideNavigationIcon = true
                        )
                    }
                    ConflictingEventResult.Holder -> replaceCertificateDialog(getEventsFromType())
                    ConflictingEventResult.None -> yourEventsViewModel.saveRemoteProtocolEvents(
                        getFlow(), getEventsFromType(), false
                    )
                }
            }
        )
    }

    private fun getEventsFromType() = when (val type = args.type) {
        is YourEventsFragmentType.DCC -> type.getRemoteEvents()
        is YourEventsFragmentType.RemoteProtocol3Type -> type.remoteEvents
    }

    private fun handleEndState(endState: YourEventsEndState) {
        when (endState) {
            is YourEventsEndState.BlockedEvent -> {
                val helpdeskPhoneNumber = cachedAppConfigUseCase.getCachedAppConfig().contactInfo.phoneNumber
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    toolbarTitle = getString(R.string.holder_listRemoteEvents_endStateCantCreateCertificate_title),
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(R.string.holder_listRemoteEvents_endStateNoValidCertificate_title),
                        descriptionData = DescriptionData(
                            htmlTextString = getString(
                                R.string.holder_listRemoteEvents_endStateNoValidCertificate_body,
                                helpdeskPhoneNumber,
                                helpdeskPhoneNumber,
                                errorCodeStringFactory.get(getFlow(), listOf(AppErrorResult(HolderStep.GetCredentialsNetworkRequest, BlockedEventException())))
                            ),
                            htmlLinksEnabled = true
                        ),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.general_toMyOverview),
                            navigationActionId = R.id.action_my_overview
                        )
                    )
                )
            }
            is YourEventsEndState.NegativeTestResultAddedAndNowAddVisitorAssessment -> {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    toolbarTitle = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_toolbar),
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_title),
                        descriptionData = DescriptionData(
                            htmlText = R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_body,
                            htmlLinksEnabled = true
                        ),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.holder_event_negativeTestEndstate_addVaccinationAssessment_button_complete),
                            navigationActionId = R.id.action_visitor_pass_input_token
                        )
                    )
                )
            }
            is YourEventsEndState.Hints -> {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    toolbarTitle = getString(R.string.certificate_created_toolbar_title),
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(R.string.certificate_created_toolbar_title),
                        descriptionData = DescriptionData(
                            htmlTextString = endState.localisedHints.joinToString("<br/><br/>"),
                            htmlLinksEnabled = true
                        ),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.general_toMyOverview),
                            navigationActionId = R.id.action_my_overview
                        )
                    ),
                    hideNavigationIcon = true
                )
            }
            is YourEventsEndState.WeCouldntMakeACertificateError -> {
                val errorCode = errorCodeStringFactory.get(
                    flow = getFlow(),
                    errorResults = listOf(
                        AppErrorResult(HolderStep.GetCredentialsNetworkRequest, endState.exception)
                    )
                )
                presentError(
                    data = ErrorResultFragmentData(
                        title = getString(R.string.holder_listRemoteEvents_endStateCantCreateCertificate_title),
                        description = getString(
                            R.string.holder_listRemoteEvents_endStateCantCreateCertificate_message,
                            yourEventsEndStateUtil.getErrorStateSubstring(requireContext(), getFlow()),
                            errorCode
                        ),
                        buttonTitle = getString(R.string.general_toMyOverview),
                        buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                    )
                )
            }
            is YourEventsEndStateWithCustomTitle -> {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    toolbarTitle = if (endState is YourEventsEndStateWithCustomTitle.RecoveryTooOld ||
                                endState is YourEventsEndStateWithCustomTitle.NoRecoveryButDosisCorrection ||
                                endState is YourEventsEndStateWithCustomTitle.RecoveryAndDosisCorrection
                ) { getString(R.string.your_positive_test_toolbar_title) } else {
                    getString(R.string.certificate_created_toolbar_title)
                },
                    data = InfoFragmentData.TitleDescriptionWithButton(
                        title = getString(endState.title),
                        descriptionData = DescriptionData(
                            htmlTextString = getString(endState.description),
                            htmlLinksEnabled = true
                        ),
                        primaryButtonData = ButtonData.NavigationButton(
                            text = getString(R.string.general_toMyOverview),
                            navigationActionId = R.id.action_my_overview
                        )
                    ),
                    hideNavigationIcon = true
                )
            }
            else -> {
                navigateSafety(YourEventsFragmentDirections.actionMyOverview())
            }
        }
    }

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
                findNavControllerSafety()?.popBackStack()
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
            event.negativeTest?.sampleDate?.formatDayMonthYearTime(requireContext()) ?: ""

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
            yourEventsViewModel.checkForConflictingEvents(
                remoteProtocols = getEventsFromType()
            )
        }
        binding.bottom.setButtonText(
            getString(
                when (getFlow()) {
                    HolderFlow.VaccinationAssessment -> {
                        R.string.holder_event_vaccination_assessment_action_title
                    }
                    else -> {
                        R.string.your_negative_test_results_row_button
                    }
                }
            )
        )
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
                            findNavControllerSafety()?.popBackStack()
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
