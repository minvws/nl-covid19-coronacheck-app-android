/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCertificateCreatedBinding
import nl.rijksoverheid.ctr.holder.modules.qualifier.LoginQualifier
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.LoginViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.core.qualifier.named


class CertificateCreatedFragment :
    Fragment(R.layout.fragment_certificate_created) {

    private val args: CertificateCreatedFragmentArgs by navArgs()
    protected val digidViewModel: LoginViewModel by sharedViewModel(
        qualifier = named(LoginQualifier.DIGID)
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCertificateCreatedBinding.bind(view)
        binding.bottom.setButtonClick { backToOverview() }
        binding.retrieveTestButton.setOnClickListener {
            navigateSafety(
                CertificateCreatedFragmentDirections.actionGetEvents(
                    originType = RemoteOriginType.Recovery,
                    afterIncompleteVaccination = true,
                    toolbarTitle = resources.getString(R.string.retrieve_test_result_toolbar_title)
                )
            )
        }
        with(args) {
            binding.title.text = title
            binding.description.setHtmlText(
                htmlText = description,
                htmlLinksEnabled = true,
            )
            binding.retrieveTestButton.visibility =
                if (shouldShowRetrieveTestButton) View.VISIBLE else View.GONE
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backToOverview()
            }
        })
    }

    private fun backToOverview() {
        // Clear the token when the DigiD flow is finished
        digidViewModel.clearAccessToken()
        findNavControllerSafety()?.popBackStack()
    }
}