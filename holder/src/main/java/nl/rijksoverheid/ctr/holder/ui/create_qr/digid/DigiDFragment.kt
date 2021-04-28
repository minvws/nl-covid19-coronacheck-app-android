package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import net.openid.appauth.AuthorizationService
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class DigiDFragment : Fragment(0) {

    protected val digidViewModel: DigiDViewModel by viewModel()
    private val authService by lazy { AuthorizationService(requireActivity()) }

    private val loginResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            digidViewModel.handleActivityResult(it, authService)
        }

    fun login() {
        digidViewModel.login(loginResult, authService)
    }
}
