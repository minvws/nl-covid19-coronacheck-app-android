/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardAddQrCardAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardPlaceHolderAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardInfoCardAdapterItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogResult
import nl.rijksoverheid.ctr.holder.dashboard.util.CardItemUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.DashboardPageInfoItemHandlerUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.RemovedEventsBottomSheetUtil
import nl.rijksoverheid.ctr.holder.databinding.FragmentDashboardPageBinding
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getParcelableCompat
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ViewModelOwner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DashboardPageFragment : Fragment(R.layout.fragment_dashboard_page) {

    companion object {
        const val EXTRA_GREEN_CARD_TYPE = "GREEN_CARD_TYPE"
        const val EXTRA_RETURN_URI = "RETURN_URI"

        fun getInstance(
            greenCardType: GreenCardType,
            returnUri: String?
        ): DashboardPageFragment {
            val fragment = DashboardPageFragment()
            val arguments = Bundle()
            arguments.putParcelable(EXTRA_GREEN_CARD_TYPE, greenCardType)
            arguments.putString(EXTRA_RETURN_URI, returnUri)
            fragment.arguments = arguments
            return fragment
        }
    }

    private val dashboardPageInfoItemHandlerUtil: DashboardPageInfoItemHandlerUtil by inject()
    private val removedEventsBottomSheetUtil: RemovedEventsBottomSheetUtil by inject()
    private val cardItemUtil: CardItemUtil by inject()
    private val dialogUtil: DialogUtil by inject()
    val dashboardViewModel: DashboardViewModel by sharedViewModelWithOwner(owner = {
        ViewModelOwner.from(
            requireParentFragment()
        )
    })
    val section = Section()
    private val greenCardType: GreenCardType by lazy {
        arguments?.getParcelableCompat(
            EXTRA_GREEN_CARD_TYPE
        ) ?: error("EXTRA_GREEN_CARD_TYPE should not be null")
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<RecyclerView>(R.id.recyclerView)?.let {
            dashboardViewModel.scrollUpdate(it.canScrollVertically(RecyclerView.SCROLL_AXIS_VERTICAL), greenCardType)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDashboardPageBinding.bind(view)
        initRecyclerView(binding)
        observeItem()
        observeShowBlockedEventsDialog()
    }

    private fun observeItem() {
        dashboardViewModel.dashboardTabItemsLiveData.observe(viewLifecycleOwner) {
            setItems(
                myDashboardItems = it.firstOrNull { items -> items.greenCardType == greenCardType }?.items
                    ?: listOf()
            )
        }
    }

    private fun observeShowBlockedEventsDialog() {
        dashboardViewModel.showBlockedEventsDialogLiveData.observe(
            viewLifecycleOwner,
            EventObserver { result ->
                when (result) {
                    is ShowBlockedEventsDialogResult.Show -> {
                        dialogUtil.presentDialog(
                            context = requireContext(),
                            title = R.string.holder_invaliddetailsremoved_alert_title,
                            message = getString(R.string.holder_invaliddetailsremoved_alert_body),
                            positiveButtonText = R.string.holder_invaliddetailsremoved_alert_button_close,
                            positiveButtonCallback = { },
                            negativeButtonText = R.string.holder_invaliddetailsremoved_alert_button_moreinfo,
                            negativeButtonCallback = { removedEventsBottomSheetUtil.presentBlockedEvents(this, result.blockedEvents) }
                        )
                    }
                    ShowBlockedEventsDialogResult.None -> {
                        /* nothing */
                    }
                }
            }
        )
    }

    private fun initRecyclerView(binding: FragmentDashboardPageBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                dashboardViewModel.scrollUpdate(
                    canScrollVertically = recyclerView.canScrollVertically(RecyclerView.SCROLL_AXIS_VERTICAL),
                    greenCardType = greenCardType
                )
            }
        })
    }

    private fun setItems(
        myDashboardItems: List<DashboardItem>
    ) {
        val adapterItems = mutableListOf<BindableItem<*>>()
        myDashboardItems.forEach { dashboardItem ->
            when (dashboardItem) {
                is DashboardItem.HeaderItem -> addHeader(adapterItems, dashboardItem)
                is DashboardItem.PlaceholderCardItem -> addPlaceHolder(adapterItems, dashboardItem)
                is DashboardItem.CardsItem -> addCards(adapterItems, dashboardItem)
                is DashboardItem.AddQrButtonItem -> {
                    // Handled by MyOverviewTabsFragment
                }
                is DashboardItem.InfoItem -> addInfoCard(adapterItems, dashboardItem)
                is DashboardItem.AddQrCardItem -> addAddQrCardItem(adapterItems)
            }
        }

        section.update(adapterItems)
    }

    private fun addAddQrCardItem(adapterItems: MutableList<BindableItem<*>>) {
        adapterItems.add(
            DashboardAddQrCardAdapterItem(
                onButtonClick = {
                    findNavControllerSafety()?.navigate(DashboardPageFragmentDirections.actionChooseProofType())
                })
        )
    }

    private fun addInfoCard(
        adapterItems: MutableList<BindableItem<*>>,
        dashboardItem: DashboardItem.InfoItem
    ) {
        adapterItems.add(DashboardInfoCardAdapterItem(
            infoItem = dashboardItem,
            onButtonClick = {
                dashboardPageInfoItemHandlerUtil.handleButtonClick(this, it)
            },
            onDismiss = { infoCardItem, infoItem ->
                dashboardPageInfoItemHandlerUtil.handleDismiss(
                    this,
                    infoCardItem,
                    infoItem
                )
            }
        ))
    }

    private fun addCards(
        adapterItems: MutableList<BindableItem<*>>,
        dashboardItem: DashboardItem.CardsItem
    ) {
        adapterItems.add(
            DashboardGreenCardAdapterItem(
                cards = dashboardItem.cards,
                onButtonClick = { cardItem, credentialsWithExpirationTime ->
                    navigateSafety(
                        DashboardPageFragmentDirections.actionQrCode(
                            toolbarTitle = when (cardItem.greenCard.greenCardEntity.type) {
                                is GreenCardType.Domestic -> {
                                    getString(cardItemUtil.getQrCodesFragmentToolbarTitle(cardItem))
                                }
                                is GreenCardType.Eu -> {
                                    getString(R.string.my_overview_test_result_international_title)
                                }
                            }, data = QrCodeFragmentData(
                                shouldDisclose = cardItemUtil.shouldDisclose(cardItem),
                                credentialsWithExpirationTime = credentialsWithExpirationTime,
                                type = cardItem.greenCard.greenCardEntity.type,
                                originType = cardItem.greenCard.origins.first().type
                            ), returnUri = arguments?.getString(EXTRA_RETURN_URI)
                        )
                    )
                },
                onRetryClick = {
                    dashboardViewModel.refresh(
                        dashboardSync = DashboardSync.ForceSync
                    )
                },
                onCountDownFinished = {
                    dashboardViewModel.refresh()
                }
            )
        )
    }

    private fun addPlaceHolder(
        adapterItems: MutableList<BindableItem<*>>,
        dashboardItem: DashboardItem.PlaceholderCardItem
    ) {
        adapterItems.add(
            DashboardGreenCardPlaceHolderAdapterItem(
                greenCardType = dashboardItem.greenCardType
            )
        )
    }

    private fun addHeader(
        adapterItems: MutableList<BindableItem<*>>,
        dashboardItem: DashboardItem.HeaderItem
    ) {
        adapterItems.add(
            DashboardHeaderAdapterItem(
                text = dashboardItem.text,
                buttonInfo = dashboardItem.buttonInfo
            )
        )
    }
}
