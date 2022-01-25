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
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.databinding.FragmentMenuBinding

class MenuFragment: Fragment(R.layout.item_menu_section_footer) {

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
        binding.recyclerView.adapter = GroupieAdapter()
        binding.recyclerView.itemAnimator = null
    }
}