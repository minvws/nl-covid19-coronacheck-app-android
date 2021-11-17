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
import nl.rijksoverheid.ctr.holder.databinding.FragmentInternationalCertificateCreatedBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety


class InternationalCertificateCreatedFragment :
    Fragment(R.layout.fragment_international_certificate_created) {

    private val args: InternationalCertificateCreatedFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentInternationalCertificateCreatedBinding.inflate(
            inflater, container, false
        ).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentInternationalCertificateCreatedBinding.bind(view)
        binding.bottom.setButtonClick {
            navigateSafety(InternationalCertificateCreatedFragmentDirections.actionMyOverview())
        }
        binding.retrieveTestButton.setOnClickListener {
            navigateSafety(
                InternationalCertificateCreatedFragmentDirections.actionGetEvents(
                    originType = OriginType.Recovery,
                    afterIncompleteVaccination = true
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