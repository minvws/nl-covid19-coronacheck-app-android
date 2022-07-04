/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.no_digid

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDigidBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NoDigidFragment : Fragment(R.layout.fragment_no_digid) {

    private val args: NoDigidFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoDigidBinding.bind(view)

        with(args.data) {
            binding.title.text = title
            binding.description.text = description

            binding.firstButton.bind(
                title = firstButtonData.title,
                subtitle = firstButtonData.subtitle,
                logo = firstButtonData.icon
            ) {
                println("1")
            }

            binding.secondButton.bind(
                title = secondButtonData.title,
                subtitle = secondButtonData.subtitle,
                logo = secondButtonData.icon
            ) {
                println("2")
            }
        }
    }
}
