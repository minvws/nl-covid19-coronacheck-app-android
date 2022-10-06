package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentHolderNameSelectionBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionFragment : Fragment(R.layout.fragment_holder_name_selection) {
    private val section = Section()
    private val viewModel: HolderNameSelectionViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHolderNameSelectionBinding.bind(view)
        initRecyclerView(binding)
        viewModel.itemsLiveData.observe(viewLifecycleOwner, ::setItems)
    }

    private fun initRecyclerView(binding: FragmentHolderNameSelectionBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun setItems(items: List<HolderNameSelectionItem>) {
        section.update(
            items.map {
                when (it) {
                    HolderNameSelectionItem.FooterItem -> HolderNameSelectionFooterAdapterItem()
                    HolderNameSelectionItem.HeaderItem -> HolderNameSelectionHeaderAdapterItem()
                    is HolderNameSelectionItem.ListItem -> HolderNameSelectionViewAdapterItem(it)
                }
            }
        )
    }
}
