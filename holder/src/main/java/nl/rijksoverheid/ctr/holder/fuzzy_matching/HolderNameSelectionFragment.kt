package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentHolderNameSelectionBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import org.koin.android.ext.android.inject
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

    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val selectionDetailBottomSheetDescriptionUtil: SelectionDetailBottomSheetDescriptionUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHolderNameSelectionBinding.bind(view)
        initRecyclerView(binding)
        addToolbarButton()
        viewModel.itemsLiveData.observe(viewLifecycleOwner) {
            setItems(it, binding)
        }
        binding.bottom.setButtonClick {
            if (viewModel.noSelectionYet()) {
                binding.bottom.showError()
            } else {
                // TODO store selected name and discard the others
                findNavControllerSafety()?.popBackStack()
            }
        }
    }

    private fun addToolbarButton() {
        (parentFragment?.parentFragment as? HolderMainFragment)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.fuzzy_matching_toolbar)

                    setOnMenuItemClickListener {
                        if (it.itemId == R.id.skip) {
                            // close fuzzy matching
                            findNavController().popBackStack(R.id.nav_holder_fuzzy_matching, true)
                        }
                        true
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (parentFragment?.parentFragment as? HolderMainFragment)?.let {
            it.getToolbar().menu.clear()
            // Reset menu item listener to default
            it.resetMenuItemListener()
        }
    }

    private fun initRecyclerView(binding: FragmentHolderNameSelectionBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setItems(
        items: List<HolderNameSelectionItem>,
        binding: FragmentHolderNameSelectionBinding
    ) {
        section.update(
            items.map { item ->
                when (item) {
                    HolderNameSelectionItem.FooterItem -> HolderNameSelectionFooterAdapterItem {
                        infoFragmentUtil.presentAsBottomSheet(
                            fragmentManager = parentFragmentManager,
                            data = InfoFragmentData.TitleDescription(
                                title = getString(R.string.holder_fuzzyMatching_why_title),
                                descriptionData = DescriptionData(
                                    htmlText = R.string.holder_fuzzyMatching_why_body,
                                    htmlLinksEnabled = true
                                )
                            )
                        )
                    }
                    HolderNameSelectionItem.HeaderItem -> HolderNameSelectionHeaderAdapterItem()
                    is HolderNameSelectionItem.ListItem -> HolderNameSelectionViewAdapterItem(
                        item,
                        {
                            val nameText =
                                getString(R.string.holder_identitySelection_details_body, item.name)
                            val eventsText = selectionDetailBottomSheetDescriptionUtil.get(
                                selectionDetailData = item.detailData,
                                separator = " ${getString(R.string.general_and)} "
                            ) {
                                getString(
                                    R.string.holder_storedEvents_listHeader_fetchedFromProvider,
                                    it
                                )
                            }
                            infoFragmentUtil.presentAsBottomSheet(
                                fragmentManager = parentFragmentManager,
                                data = InfoFragmentData.TitleDescription(
                                    title = getString(R.string.general_details),
                                    descriptionData = DescriptionData(
                                        htmlTextString = "$nameText $eventsText",
                                        htmlLinksEnabled = true
                                    )
                                )
                            )
                        }) { index ->
                        binding.bottom.hideError()
                        viewModel.onItemSelected(index)
                    }
                }
            }
        )
    }
}
