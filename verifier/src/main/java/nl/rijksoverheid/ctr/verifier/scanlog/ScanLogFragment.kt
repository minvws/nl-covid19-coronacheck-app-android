package nl.rijksoverheid.ctr.verifier.scanlog

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanLogBinding
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogEmptyListAdapterItem
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogFirstInstallTimeAdapterItem
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogHeaderAdapterItem
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogItem
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogListAdapterItem
import nl.rijksoverheid.ctr.verifier.scanlog.items.ScanLogListHeaderAdapterItem
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanLogFragment : Fragment(R.layout.fragment_scan_log) {

    private val scanLogViewModel: ScanLogViewModel by viewModel()
    private val section = Section()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentScanLogBinding.bind(view)
        initRecyclerView(binding)
        observeItems()
        scanLogViewModel.getItems()
    }

    private fun initRecyclerView(binding: FragmentScanLogBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
    }

    private fun observeItems() {
        scanLogViewModel.scanLogItemsLiveData.observe(viewLifecycleOwner) {
            setItems(
                scanLogItems = it
            )
        }
    }

    private fun setItems(
        scanLogItems: List<ScanLogItem>
    ) {
        val adapterItems = mutableListOf<BindableItem<*>>()
        scanLogItems.forEach { scanLogItem ->
            when (scanLogItem) {
                is ScanLogItem.HeaderItem -> adapterItems.add(ScanLogHeaderAdapterItem(scanLogItem))
                is ScanLogItem.ListHeaderItem -> adapterItems.add(ScanLogListHeaderAdapterItem(scanLogItem))
                is ScanLogItem.ListScanLogItem -> adapterItems.add(ScanLogListAdapterItem(scanLogItem))
                is ScanLogItem.ListEmptyItem -> adapterItems.add(ScanLogEmptyListAdapterItem())
                is ScanLogItem.FirstInstallTimeItem -> adapterItems.add(ScanLogFirstInstallTimeAdapterItem(scanLogItem))
            }
        }

        section.update(adapterItems)
    }
}
