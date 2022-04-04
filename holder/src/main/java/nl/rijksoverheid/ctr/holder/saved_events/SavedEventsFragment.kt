/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.saved_events

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentSavedEventsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SavedEventsFragment: Fragment(R.layout.fragment_saved_events) {

    private val savedEventsViewModel: SavedEventsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSavedEventsBinding.bind(view)
        savedEventsViewModel.getSavedEvents()

        savedEventsViewModel.savedEventsLiveData.observe(viewLifecycleOwner) {
            Timber.v("Amount of events: " + it.size)
        }
    }
}