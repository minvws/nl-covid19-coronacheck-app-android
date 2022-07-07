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
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNoDigidBinding
import nl.rijksoverheid.ctr.holder.get_events.DigiDFragment
import nl.rijksoverheid.ctr.holder.ui.create_qr.bind
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NoDigidFragment : DigiDFragment(R.layout.fragment_no_digid) {

    private val args: NoDigidFragmentArgs by navArgs()
    private val intentUtil: IntentUtil by inject()
    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onButtonClickWithRetryAction() {
        loginWithDigiD()
    }

    override fun onButtonClickWithRetryTitle(): Int {
        return super.onButtonClickWithRetryTitle()
    }

    override fun getFlow(): Flow {
        return args.data.flow
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNoDigidBinding.bind(view)

        with(args.data) {
            binding.title.text = title
            if (description.isNotEmpty()) {
                binding.description.text = description
            } else {
                binding.description.visibility = View.GONE
            }

            binding.firstButton.bind(
                title = firstNavigationButtonData.title,
                subtitle = firstNavigationButtonData.subtitle,
                logo = firstNavigationButtonData.icon
            ) {
                onButtonClick(firstNavigationButtonData)
            }

            binding.secondButton.bind(
                title = secondNavigationButtonData.title,
                subtitle = secondNavigationButtonData.subtitle,
                logo = secondNavigationButtonData.icon
            ) {
                onButtonClick(secondNavigationButtonData)
            }

            if (args.data.firstNavigationButtonData is NoDigidNavigationButtonData.Ggd) {
                digidViewModel.loading.observe(viewLifecycleOwner, EventObserver {
                    (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
                })
            }
        }
    }
    
    private fun onButtonClick(data: NoDigidNavigationButtonData) {
        when (data) {
            is NoDigidNavigationButtonData.NoDigid -> {
                navigateSafety(
                    NoDigidFragmentDirections.actionNoDigid(data.noDigidFragmentData)
                )
            }
            is NoDigidNavigationButtonData.Info -> {
                infoFragmentUtil.presentFullScreen(
                    currentFragment = this,
                    toolbarTitle = getString(R.string.choose_provider_toolbar),
                    data = data.infoFragmentData
                )
            }
            is NoDigidNavigationButtonData.Link -> {
                intentUtil.openUrl(
                    context = requireContext(),
                    url = data.externalUrl,
                )
            }
            is NoDigidNavigationButtonData.Ggd -> {
                loginWithDigiD()
            }
        }
    }
}
