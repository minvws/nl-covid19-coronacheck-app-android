/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.get_events

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.BrowserSelector
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.EventProvider
import nl.rijksoverheid.ctr.holder.get_events.models.EventsResult
import nl.rijksoverheid.ctr.holder.modules.qualifier.LoginQualifier
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DigiDFragment(contentLayoutId: Int) : BaseFragment(contentLayoutId) {

    private val dialogUtil: DialogUtil by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val getEventsViewModel: GetEventsViewModel by viewModel()
    protected val digidViewModel: LoginViewModel by sharedViewModel(named(LoginQualifier.DIGID))
    protected val mijnCnViewModel: LoginViewModel by viewModel(named(LoginQualifier.MIJN_CN))
    private val authService by lazy {
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setBrowserMatcher(BrowserAllowList(*getSupportedBrowsers()))
            .build()
        AuthorizationService(requireActivity(), appAuthConfig)
    }

    private val loginResultMijnCn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            mijnCnViewModel.handleActivityResult(it, authService)
        }

    private val authServiceMijnCn by lazy {
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setBrowserMatcher(BrowserAllowList(*getSupportedBrowsers()))
            .build()
        AuthorizationService(requireActivity(), appAuthConfig)
    }

    private val loginResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            digidViewModel.handleActivityResult(it, authService)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val copy = getCopyForOriginType()

        getEventsViewModel.eventsResult.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is EventsResult.Success -> {
                    if (it.missingEvents) {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = getDialogTitleFromOriginType(getOriginTypes().first()),
                            message = getString(R.string.error_get_events_missing_events_dialog_description),
                            positiveButtonText = R.string.dialog_close,
                            positiveButtonCallback = {},
                            onDismissCallback = {
                                onNavigateToYourEvents(
                                    remoteProtocols = it.remoteEvents,
                                    eventProviders = it.eventProviders
                                )
                            }
                        )
                    } else {
                        onNavigateToYourEvents(
                            remoteProtocols = it.remoteEvents,
                            eventProviders = it.eventProviders
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
                        val navDirections = InfoFragmentDirections.actionMyOverview()
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
                                        navigationActionId = navDirections.actionId,
                                        navigationArguments = navDirections.arguments
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

        digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            onDigidLoading(it)
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })

        digidViewModel.loginResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is LoginResult.Success -> {
                    getEventsViewModel.getDigidEvents(
                        it.jwt,
                        getOriginTypes()
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

        getEventsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            onGetEventsLoading(it)
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
        })
    }

    fun loginWithDigiD() {
        digidViewModel.login(loginResult, authService)
    }

    fun loginWithMijnCN() {
        mijnCnViewModel.login(loginResultMijnCn, authServiceMijnCn)
    }

    protected fun getErrorCodes(errorResults: List<ErrorResult>): String {
        return errorCodeStringFactory.get(
            flow = getFlow(),
            errorResults = errorResults
        )
    }

    protected fun getDialogTitleFromOriginType(originType: RemoteOriginType): Int {
        return when (originType) {
            RemoteOriginType.Recovery -> R.string.error_get_events_missing_events_dialog_title_recoveries
            RemoteOriginType.Test -> R.string.error_get_events_missing_events_dialog_title_testresults
            RemoteOriginType.Vaccination -> R.string.error_get_events_missing_events_dialog_title_vaccines
        }
    }

    /**
     * Gets all supported browsers and filters out the custom tab browsers as those can cause
     * issues with DigiD
     *
     * @return Array of browser matchers supported for the app auth config
     */
    private fun getSupportedBrowsers(): Array<VersionedBrowserMatcher> =
        BrowserSelector.getAllBrowsers(context)
            .filter { it.useCustomTab == false }
            .filter { it.packageName != "android" }
            .map {
                VersionedBrowserMatcher(
                    it.packageName,
                    it.signatureHashes,
                    false,
                    VersionRange.ANY_VERSION
                )
            }.toTypedArray()

    protected fun getCopyForOriginType(): GetEventsFragmentCopy {
        when (getOriginTypes().first()) {
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

    abstract fun onDigidLoading(loading: Boolean)
    abstract fun onGetEventsLoading(loading: Boolean)
    abstract fun getOriginTypes(): List<RemoteOriginType>
    abstract fun onNavigateToYourEvents(
        remoteProtocols: Map<RemoteProtocol, ByteArray>,
        eventProviders: List<EventProvider> = emptyList()
    )
}
