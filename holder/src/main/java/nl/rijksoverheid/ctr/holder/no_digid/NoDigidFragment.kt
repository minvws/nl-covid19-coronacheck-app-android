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
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDigidBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NoDigidFragment : Fragment(R.layout.fragment_no_digid) {

    private val args: NoDigidFragmentArgs by navArgs()
    private val intentUtil: IntentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoDigidBinding.bind(view)

        with(args.data) {
            binding.title.text = title
            binding.description.text = description

            binding.firstButton.bind(
                title = firstNavigationButtonData.title,
                subtitle = firstNavigationButtonData.subtitle,
                logo = firstNavigationButtonData.icon
            ) {
                firstNavigationButtonData.buttonClickDirection?.let {
                    navigateSafety(
                        destinationId = it.actionId,
                        args = it.arguments
                    )
                }
                firstNavigationButtonData.externalUrl?.let {
                    intentUtil.openUrl(
                        context = requireContext(),
                        url = it,
                    )
                }
            }

            binding.secondButton.bind(
                title = secondNavigationButtonData.title,
                subtitle = secondNavigationButtonData.subtitle,
                logo = secondNavigationButtonData.icon
            ) {
                secondNavigationButtonData.buttonClickDirection?.let {
                    navigateSafety(
                        destinationId = it.actionId,
                        args = it.arguments
                    )
                }
            }
        }
    }
}
