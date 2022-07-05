/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.databinding.WidgetScrollViewCheckboxButtonBinding
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetEventsBinding
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.EventsResult
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.no_digid.*
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetEventsFragment : DigiDFragment(R.layout.fragment_get_events) {

    private val args: GetEventsFragmentArgs by navArgs()
    private val dialogUtil: DialogUtil by inject()
    private val featureFlagUseCase: HolderFeatureFlagUseCase by inject()
    private val getEventsViewModel: GetEventsViewModel by viewModel()
    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val noDigidScreenDataUtil: NoDigidScreenDataUtil by inject()

    override fun onButtonClickWithRetryAction() {
        loginWithDigiD()
    }

    override fun getFlow(): Flow {
        return when (args.originType) {
            RemoteOriginType.Recovery -> HolderFlow.Recovery
            RemoteOriginType.Test -> HolderFlow.DigidTest
            RemoteOriginType.Vaccination -> HolderFlow.Vaccination
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentGetEventsBinding.bind(view)
        val checkboxButtonBinding = WidgetScrollViewCheckboxButtonBinding.bind(binding.root)
        val copy = getCopyForOriginType()
        setBindings(binding, copy)
        setObservers(binding, copy, checkboxButtonBinding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }

    private fun setObservers(
        binding: FragmentGetEventsBinding,
        copy: GetEventsFragmentCopy,
        checkboxButtonBinding: WidgetScrollViewCheckboxButtonBinding
    ) {
        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.button.isEnabled = !it
            checkboxButtonBinding.checkbox.isEnabled = !it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            binding.button.isEnabled = !it
            checkboxButtonBinding.checkbox.isEnabled = !it
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        getEventsViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = getDialogTitleFromOriginType(args.originType),
                            message = getString(R.string.error_get_events_missing_events_dialog_description),
                            positiveButtonText = R.string.dialog_close,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                navigateToYourEvents(
                                    remoteProtocols = it.remoteEvents,
                                    eventProviders = it.eventProviders,
                                    getPositiveTestWithVaccination = checkboxButtonBinding.checkbox.isChecked
                                )
                            }
                        )
                    } else {
                        navigateToYourEvents(
                            remoteProtocols = it.remoteEvents,
                            eventProviders = it.eventProviders,
                            getPositiveTestWithVaccination = checkboxButtonBinding.checkbox.isChecked
                        )
                    }
                }
                is EventsResult.HasNoEvents -> {
                    if (it.missingEvents) {
                        presentError(
                            data = ErrorResultFragmentData(
                                title = getString(R.string.error_get_events_no_events_title),
                                description = getString(
                                    R.string.error_get_events_http_error_description,
                                    getErrorCodes(it.errorResults)
                                ),
                                buttonTitle = getString(R.string.back_to_overview),
                                ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview),
                                urlData = ErrorResultFragmentData.UrlData(
                                    urlButtonTitle = getString(R.string.error_something_went_wrong_outage_button),
                                    urlButtonUrl = getString(R.string.error_something_went_wrong_outage_button_url)
                                ),
                            )
                        )
                    } else {
                        val test = InfoFragmentDirections.actionMyOverview()
                        infoFragmentUtil.presentFullScreen(
                            currentFragment = this,
                            infoFragmentDirections = GetEventsFragmentDirections.actionInfoFragment(
                                toolbarTitle = copy.toolbarTitle,
                                data = InfoFragmentData.TitleDescriptionWithButton(
                                    title = copy.hasNoEventsTitle,
                                    descriptionData = DescriptionData(
                                        htmlTextString = copy.hasNoEventsDescription,
                                        htmlLinksEnabled = true
                                    ),
                                    primaryButtonData = ButtonData.NavigationButton(
                                        text = getString(R.string.back_to_overview),
                                        navigationActionId = test.actionId,
                                        navigationArguments = test.arguments
                                    )
                                )
                            )
                        )
                    }
                }
                is EventsResult.Error -> {
                    when {
                        it.accessTokenSessionExpiredError() -> {
                            presentError(
                                data = ErrorResultFragmentData(
                                    title = getString(R.string.error_access_tokens_session_expired_title),
                                    description = getString(R.string.error_access_tokens_Session_expired_description),
                                    buttonTitle = getString(R.string.error_access_tokens_session_expired_button),
                                    ErrorResultFragmentData.ButtonAction.PopBackStack
                                )
                            )
                        }
                        it.accessTokenNoBsn() -> {
                            presentError(
                                data = ErrorResultFragmentData(
                                    title = getString(R.string.error_access_tokens_no_bsn_title),
                                    description = getString(R.string.error_access_tokens_no_bsn_description),
                                    buttonTitle = getString(R.string.back_to_overview),
                                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(
                                        R.id.action_my_overview
                                    )
                                )
                            )
                        }
                        it.unomiOrEventErrors() -> {
                            presentError(
                                it.errorResults.first(),
                                getString(
                                    R.string.error_get_events_http_error_description,
                                    getErrorCodes(it.errorResults)
                                )
                            )
                        }
                        else -> {
                            presentError(it.errorResults.first())
                        }
                    }
                }
            }
        })

        digidViewModel.loginResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is LoginResult.Success -> {
                    getEventsViewModel.getDigidEvents(
                        it.jwt,
                        args.originType,
                        checkboxButtonBinding.checkbox.isChecked
                    )
                }
                is LoginResult.Failed -> {
                    presentError(it.errorResult)
                }
                is LoginResult.Cancelled -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.digid_login_cancelled_title,
                        message = getString(R.string.digid_login_cancelled_description),
                        positiveButtonText = R.string.dialog_close,
                        positiveButtonCallback = {}
                    )
                }
                LoginResult.TokenUnavailable -> {
                    binding.root.visibility = View.VISIBLE
                }
                LoginResult.NoBrowserFound -> {
                    dialogUtil.presentDialog(
                        context = requireContext(),
                        title = R.string.dialog_no_browser_title,
                        message = getString(R.string.dialog_no_browser_message_digid),
                        positiveButtonText = R.string.ok,
                        positiveButtonCallback = {},
                    )
                }
            }
        })
    }

    private fun setBindings(
        binding: FragmentGetEventsBinding,
        copy: GetEventsFragmentCopy
    ) {
        binding.title.text = copy.title
        binding.description.setHtmlText(copy.description, htmlLinksEnabled = true)
        binding.button.setOnClickListener {
            onButtonClickWithRetryAction()
        }
        binding.noDigidButton.setOnClickListener {
            if (featureFlagUseCase.getMijnCnEnabled() &&
                args.originType == RemoteOriginType.Vaccination
            ) {
                navigateSafety(GetEventsFragmentDirections.actionMijnCn())
            } else {
                navigateSafety(GetEventsFragmentDirections.actionNoDigid(
                    NoDigidFragmentData(
                        title = getString(R.string.holder_noDigiD_title),
                        description = getString(R.string.holder_noDigiD_message),
                        firstNavigationButtonData = noDigidScreenDataUtil.requestDigidButton(getFlow()),
                        secondNavigationButtonData = noDigidScreenDataUtil.continueWithoutDigidButton(getFlow())
                )))
            }
        }
        binding.checkboxContainer.isVisible = args.originType == RemoteOriginType.Vaccination
    }

    private fun getCopyForOriginType(): GetEventsFragmentCopy {
        when (args.originType) {
            is RemoteOriginType.Test -> {
                return GetEventsFragmentCopy(
                    title = getString(R.string.holder_negativetest_ggd_title),
                    description = getString(R.string.holder_negativetest_ggd_message),
                    toolbarTitle = getString(R.string.your_negative_test_results_header),
                    hasNoEventsTitle = getString(R.string.no_test_results_title),
                    hasNoEventsDescription = getString(R.string.no_test_results_description)
                )
            }
            is RemoteOriginType.Vaccination -> {
                return GetEventsFragmentCopy(
                    title = getString(R.string.holder_addVaccination_title),
                    description = getString(R.string.holder_addVaccination_message),
                    toolbarTitle = getString(R.string.your_vaccination_result_toolbar_title),
                    hasNoEventsTitle = getString(R.string.no_vaccinations_title),
                    hasNoEventsDescription = getString(R.string.no_vaccinations_description)
                )
            }
            is RemoteOriginType.Recovery -> {
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

    private fun navigateToYourEvents(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        eventProviders: List<EventProvider> = emptyList(),
        getPositiveTestWithVaccination: Boolean
    ) {
        val flow = getFlow()
        navigateSafety(
            GetEventsFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = remoteProtocols,
                    eventProviders = eventProviders
                ),
                toolbarTitle = getCopyForOriginType().toolbarTitle,
                flow = if (flow == HolderFlow.Vaccination && getPositiveTestWithVaccination) {
                    HolderFlow.VaccinationAndPositiveTest
                } else {
                    flow
                }
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