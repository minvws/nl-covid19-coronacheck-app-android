/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.sync_greencards

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentSyncGreenCardsBinding
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SyncGreenCardsFragment : BaseFragment(R.layout.fragment_sync_green_cards) {

    private val args: SyncGreenCardsFragmentArgs by navArgs()
    private val syncGreenCardsViewModel: SyncGreenCardsViewModel by viewModel()

    override fun onButtonClickWithRetryAction() {
        syncGreenCardsViewModel.refresh()
    }

    override fun getFlow(): Flow = HolderFlow.SyncGreenCards

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSyncGreenCardsBinding.bind(view)
        binding.title.text = args.title
        binding.description.setHtmlText(args.description)
        binding.bottom.setButtonText(args.button)

        binding.bottom.setButtonClick {
            onButtonClickWithRetryAction()
        }

        syncGreenCardsViewModel.loading.observe(viewLifecycleOwner, EventObserver {
            (parentFragment?.parentFragment as HolderMainFragment).presentLoading(it)
            binding.bottom.setButtonEnabled(!it)
        })

        syncGreenCardsViewModel.databaseSyncerResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DatabaseSyncerResult.Success -> {
                    navigateSafety(
                        SyncGreenCardsFragmentDirections.actionMyOverview()
                    )
                }
                is DatabaseSyncerResult.Failed -> {
                    presentError(
                        errorResult = it.errorResult
                    )
                }
            }
        })
    }
}
