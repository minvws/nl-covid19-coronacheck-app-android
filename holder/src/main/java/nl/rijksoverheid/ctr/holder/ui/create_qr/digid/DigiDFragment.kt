package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import androidx.activity.result.contract.ActivityResultContracts
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthorizationService
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.BrowserSelector
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
abstract class DigiDFragment(contentLayoutId: Int) : BaseFragment(contentLayoutId) {

    abstract val originType: OriginType

    protected val digidViewModel: DigiDViewModel by viewModel()
    private val authService by lazy {
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

    override fun getFlow(): Flow {
        return when (originType) {
            is OriginType.Vaccination -> HolderFlow.Vaccination
            is OriginType.Test -> HolderFlow.DigidTest
            is OriginType.Recovery -> HolderFlow.Recovery
        }
    }
}
