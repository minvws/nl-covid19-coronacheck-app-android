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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDashboardPageBinding
import nl.rijksoverheid.ctr.holder.databinding.FragmentSavedEventsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SavedEventsFragment: Fragment(R.layout.fragment_saved_events) {

    private val section = Section()
    private val savedEventsViewModel: SavedEventsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSavedEventsBinding.bind(view)
        initRecyclerView(binding)
        getSavedEvents()
    }

    private fun initRecyclerView(binding: FragmentSavedEventsBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
    }

    private fun getSavedEvents() {
        savedEventsViewModel.getSavedEvents()

        savedEventsViewModel.savedEventsLiveData.observe(viewLifecycleOwner) {
            section.add(
                SavedEventsHeaderAdapterItem()
            )
        }
    }
}