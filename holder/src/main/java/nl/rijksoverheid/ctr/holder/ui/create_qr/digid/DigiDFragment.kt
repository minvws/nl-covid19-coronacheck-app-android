package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class DigiDFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected val digidViewModel: DigiDViewModel by viewModel()
    private val authService by lazy {
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setBrowserMatcher(
                BrowserAllowList(
                    VersionedBrowserMatcher.CHROME_BROWSER,
                    VersionedBrowserMatcher.SAMSUNG_BROWSER,
                    VersionedBrowserMatcher.FIREFOX_BROWSER
                )
            )
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
}
