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

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionFragment : Fragment(R.layout.fragment_holder_name_selection) {
    private val section = Section()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHolderNameSelectionBinding.bind(view)
        initRecyclerView(binding)
        setItems()
    }

    private fun initRecyclerView(binding: FragmentHolderNameSelectionBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun setItems() {
        section.update(listOf(
            HolderNameSelectionHeaderAdapterItem(),
            HolderNameSelectionViewAdapterItem(HolderNameSelectionItem.ListItem("van Geer, Caroline Johanna Helena", "3 vaccinaties en 1 testuitslag")),
            HolderNameSelectionViewAdapterItem(HolderNameSelectionItem.ListItem("van Geer, Caroline Johanna Helena", "3 vaccinaties en 1 testuitslag")),
            HolderNameSelectionViewAdapterItem(HolderNameSelectionItem.ListItem("van Geer, Caroline Johanna Helena", "3 vaccinaties en 1 testuitslag")),
            HolderNameSelectionFooterAdapterItem()
        ))
    }
}
