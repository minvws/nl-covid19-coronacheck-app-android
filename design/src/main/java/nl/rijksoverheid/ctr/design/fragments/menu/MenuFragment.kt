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
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import org.koin.android.ext.android.inject

class MenuFragment: Fragment(R.layout.fragment_menu) {

    private val intentUtil: IntentUtil by inject()
    private val menuSections by lazy { (requireArguments().getParcelableArray("menuSections") as Array<MenuSection>).toList() }

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
                    menuSections.forEach {
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
                    lastItemInSection = index == menuSection.menuItems.size - 1,
                    onClick = {
                        handleMenuItemClick(it)
                    }
                )
            )
        }

        return Section().apply {
            addAll(adapterItems)
        }
    }

    private fun handleMenuItemClick(onClick: MenuSection.MenuItem.OnClick) {
        when (onClick) {
            is MenuSection.MenuItem.OnClick.Navigate -> {
                findNavControllerSafety()?.navigate(onClick.navigationActionId, onClick.navigationArguments)
            }
            is MenuSection.MenuItem.OnClick.OpenBrowser -> {
                intentUtil.openUrl(
                    context = requireContext(),
                    url = onClick.url
                )
            }
        }
    }
}