/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import android.os.Bundle
import android.view.View
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.sync_greencards.SyncGreenCardsViewModel
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.shared.models.Flow
import org.koin.androidx.viewmodel.ext.android.viewModel

class SavedEventsSyncGreenCardsFragment: BaseFragment(R.layout.fragment_saved_events_sync_green_cards) {

    private val syncGreenCardsViewModel: SyncGreenCardsViewModel by viewModel()

    override fun onButtonClickWithRetryAction() {
        // This screen does not have a retry button
    }

    override fun getFlow(): Flow {
       return HolderFlow.ClearEvents
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        syncGreenCardsViewModel.refresh()

        syncGreenCardsViewModel.databaseSyncerResultLiveData.observe(viewLifecycleOwner, EventObserver {
            when (it) {
                is DatabaseSyncerResult.Success -> {
                    navigateSafety(
                        SavedEventsSyncGreenCardsFragmentDirections.actionSavedEvents()
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