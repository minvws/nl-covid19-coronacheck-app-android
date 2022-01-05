package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import androidx.activity.result.contract.ActivityResultContracts
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.BrowserSelector
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.modules.LoginViewModelQualifier
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DigiDFragment(contentLayoutId: Int) : BaseFragment(contentLayoutId) {

    protected val digidViewModel: LoginViewModel by sharedViewModel(named("digid"))
    protected val mijnCnViewModel: LoginViewModel by viewModel(named("mijncn"))
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

    fun loginWithDigiD() {
        digidViewModel.login(loginResult, authService)
    }

    fun loginWithMijnCN() {
        mijnCnViewModel.login(loginResultMijnCn, authServiceMijnCn)
    }

    /**
     * Used for logging in when a session has already started with an active access token
     * to prevent having to do another DigiD login.
     */
    fun loginAgainWithDigiD() {
        digidViewModel.loginAgain()
    }

    /**
     * Remove persisted access token on expiration
     */
    fun onTokenExpired() {
        digidViewModel.clearAccessToken()
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
}
