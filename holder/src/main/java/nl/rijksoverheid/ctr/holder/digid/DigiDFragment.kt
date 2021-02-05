package nl.rijksoverheid.ctr.holder.digid

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.openid.appauth.AuthorizationService
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.ext.observeResult
import org.koin.android.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class DigiDFragment : Fragment() {

    protected val digidViewModel: DigiDViewModel by viewModel()
    private val authService by lazy { AuthorizationService(requireActivity()) }

    private val loginResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            digidViewModel.handleActivityResult(it, authService)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeResult(digidViewModel.accessTokenLiveData, {

        }, {

        }, {
            presentError()
        })
    }

    fun login() {
        digidViewModel.login(loginResult, authService)
    }

    private fun presentError() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.dialog_error_title))
            .setMessage(R.string.digid_login_failed)
            .setPositiveButton(R.string.ok) { dialog, which -> }
            .show()
    }
}
