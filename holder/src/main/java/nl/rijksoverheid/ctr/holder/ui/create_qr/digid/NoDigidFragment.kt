/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.digid

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDigidBinding
import nl.rijksoverheid.ctr.holder.launchUrl
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.shared.models.Flow

class NoDigidFragment : DigiDFragment(R.layout.fragment_no_digid) {
    override fun onButtonClickWithRetryAction() {
        loginWithMijnCN()
    }

    override fun getFlow(): Flow {
        return HolderFlow.Vaccination
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoDigidBinding.bind(view)

        binding.requestDigidButton.bind(
            R.string.no_digid_nodigid_button_title,
            getString(R.string.no_digid_nodigid_button_description),
            onClick = {
                context?.launchUrl(getString(R.string.no_digid_url))
            })

        binding.mijncnButton.bind(
            R.string.no_digid_mijncn_button_title,
            getString(R.string.no_digid_mijncn_button_description),
            onClick = {
                onButtonClickWithRetryAction()
            })
    }


}