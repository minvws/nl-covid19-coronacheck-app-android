package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentHolderNameSelectionBinding
import nl.rijksoverheid.ctr.holder.fuzzy_matching.HolderNameSelectionFragmentDirections.Companion.actionSavedEventsSyncGreenCards
import nl.rijksoverheid.ctr.holder.hideNavigationIcon
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderNameSelectionFragment : Fragment(R.layout.fragment_holder_name_selection) {
    private val section = Section()

    private val infoFragmentUtil: InfoFragmentUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    private val selectionDetailBottomSheetDescriptionUtil: SelectionDetailBottomSheetDescriptionUtil by inject()
    private val holderNameSelectionFragmentArgs: HolderNameSelectionFragmentArgs by navArgs()

    private val viewModel: HolderNameSelectionViewModel by viewModel {
        parametersOf(holderNameSelectionFragmentArgs.matchingBlobIds.ids)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHolderNameSelectionBinding.bind(view)
        initRecyclerView(binding)

        viewModel.canSkipLiveData.observe(viewLifecycleOwner) { canSkip ->
            if (canSkip) {
                addToolbarButton()
            } else {
                hideNavigationIcon()
            }
        }

        viewModel.itemsLiveData.observe(viewLifecycleOwner) {
            setItems(it, binding)
        }
        binding.bottom.setButtonClick {
            val selectedName = viewModel.selectedName()
            if (selectedName == null) {
                viewModel.nothingSelectedError()
                binding.bottom.showError()
            } else {
                viewModel.storeSelection {
                    navigateSafety(
                        actionSavedEventsSyncGreenCards(
                            selectedName = selectedName
                        )
                    )
                }
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
                            dialogUtil.presentDialog(
                                context = requireContext(),
                                title = R.string.holder_identitySelection_skipAlert_title,
                                message = getString(R.string.holder_identitySelection_skipAlert_body),
                                positiveButtonText = R.string.holder_identitySelection_skipAlert_action,
                                positiveButtonCallback = {
                                    // close fuzzy matching
                                    findNavController().popBackStack(
                                        R.id.nav_holder_fuzzy_matching,
                                        true
                                    )
                                },
                                negativeButtonText = R.string.general_cancel
                            )
                        }
                        true
                    }
                }
            }
        }
    }

    private fun resetToolbar() {
        (parentFragment?.parentFragment as? HolderMainFragment)?.let {
            it.getToolbar().menu.clear()
            // Reset menu item listener to default
            it.resetMenuItemListener()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.canSkip(holderNameSelectionFragmentArgs.getEventsFlow)
    }

    override fun onPause() {
        super.onPause()
        resetToolbar()
    }

    private fun initRecyclerView(binding: FragmentHolderNameSelectionBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addItemDecoration(
            MaterialDividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ).apply {
                isLastItemDecorated = false
            }
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
                                if (it.contains("dcc")) {
                                    getString(R.string.holder_identitySelection_details_scannedPaperProof)
                                } else {
                                    getString(
                                        R.string.holder_storedEvents_listHeader_fetchedFromProvider,
                                        it
                                    )
                                }
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
                        viewModel.onItemSelected(item.name)
                    }
                }
            }
        )
    }
}
