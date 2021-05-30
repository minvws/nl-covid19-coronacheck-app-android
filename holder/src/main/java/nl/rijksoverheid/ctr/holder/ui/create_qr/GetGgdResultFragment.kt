package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentGetVaccinationBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.digid.DigiDFragment

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class GetGgdResultFragment : DigiDFragment(R.layout.fragment_get_vaccination) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentGetVaccinationBinding.bind(view)

        binding.button.setOnClickListener {
            loginWithDigiD()
        }
    }
}

