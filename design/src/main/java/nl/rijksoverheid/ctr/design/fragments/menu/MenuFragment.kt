/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.design.fragments.menu

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentMenuBinding

class MenuFragment: Fragment(R.layout.fragment_menu) {

    private val menuItems: List<MenuSection> by lazy { requireArguments().getParcelableArrayList<MenuSection>("menuItems")?.toList()
        ?: error("menuItems should not be empty") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMenuBinding.bind(view)

        initRecyclerView(
            binding = binding
        )
    }

    private fun initRecyclerView(binding: FragmentMenuBinding) {
        binding.recyclerView.adapter =
            GroupAdapter<GroupieViewHolder>()
                .also { adapter ->
                    menuItems.forEach {
                        val section = getAdapterSectionForMenuSection(it)
                        adapter.add(section)
                    }
                }
        binding.recyclerView.itemAnimator = null
    }

    private fun getAdapterSectionForMenuSection(menuSection: MenuSection): Section {
        val adapterItems = mutableListOf<BindableItem<*>>()

        menuSection.menuItems.forEachIndexed { index, menuItem ->
            adapterItems.add(
                MenuItemAdapterItem(
                    menuItem = menuItem,
                    lastItemInSection = index == menuSection.menuItems.size - 1
                )
            )
        }

        return Section().apply {
            addAll(adapterItems)
        }
    }
}