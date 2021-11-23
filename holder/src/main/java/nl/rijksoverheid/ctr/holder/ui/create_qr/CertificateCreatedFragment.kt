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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCertificateCreatedBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety


class CertificateCreatedFragment :
    Fragment(R.layout.fragment_certificate_created) {

    private val args: CertificateCreatedFragmentArgs by navArgs()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCertificateCreatedBinding.bind(view)
        binding.bottom.setButtonClick {
            navigateSafety(CertificateCreatedFragmentDirections.actionMyOverview())
        }
        binding.retrieveTestButton.setOnClickListener {
            navigateSafety(
                CertificateCreatedFragmentDirections.actionGetEvents(
                    originType = OriginType.Recovery,
                    afterIncompleteVaccination = true,
                    toolbarTitle = resources.getString(R.string.retrieve_test_result_toolbar_title)
                )
            )
        }
        with(args) {
            binding.title.text = title
            binding.description.setHtmlText(description, htmlLinksEnabled = true)
            binding.retrieveTestButton.visibility =
                if (shouldShowRetrieveTestButton) View.VISIBLE else View.GONE
        }
    }
}