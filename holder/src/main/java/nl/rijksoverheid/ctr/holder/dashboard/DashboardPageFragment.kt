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
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.*
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.databinding.FragmentDashboardPageBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CardItemUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.DashboardViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.MyOverviewFragmentInfoItemHandlerUtil
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
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

    private val myOverviewFragmentInfoItemHandlerUtil: MyOverviewFragmentInfoItemHandlerUtil by inject()
    private val cardItemUtil: CardItemUtil by inject()
    val dashboardViewModel: DashboardViewModel by sharedViewModelWithOwner(owner = {
        ViewModelOwner.from(
            requireParentFragment()
        )
    })
    val section = Section()
    private val greenCardType: GreenCardType by lazy {
        arguments?.getParcelable<GreenCardType>(
            EXTRA_GREEN_CARD_TYPE
        ) ?: error("EXTRA_GREEN_CARD_TYPE should not be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDashboardPageBinding.bind(view)
        initRecyclerView(binding)
        observeItem()
    }

    private fun observeItem() {
        dashboardViewModel.dashboardTabItemsLiveData.observe(viewLifecycleOwner) {
            setItems(
                myDashboardItems = it.first { items -> items.greenCardType == greenCardType }.items
            )
        }
    }

    private fun initRecyclerView(binding: FragmentDashboardPageBinding) {
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
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
                is DashboardItem.CoronaMelderItem -> adapterItems.add(
                    DashboardCoronaMelderAdapterItem()
                )
                is DashboardItem.AddQrCardItem -> addAddQrCardItem(adapterItems)
            }
        }

        section.update(adapterItems)
    }

    private fun addAddQrCardItem(adapterItems: MutableList<BindableItem<*>>) {
        adapterItems.add(
            DashboardAddQrCardAdapterItem(
                onButtonClick = {
                    findNavControllerSafety()?.navigate(DashboardPageFragmentDirections.actionQrType())
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
                myOverviewFragmentInfoItemHandlerUtil.handleButtonClick(this, it)
            },
            onDismiss = { infoCardItem, infoItem ->
                myOverviewFragmentInfoItemHandlerUtil.handleDismiss(
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
                onButtonClick = { cardItem, credentials, expiration ->
                    navigateSafety(
                        DashboardPageFragmentDirections.actionQrCode(
                            toolbarTitle = when (cardItem.greenCard.greenCardEntity.type) {
                                is GreenCardType.Domestic -> {
                                    getString(R.string.domestic_qr_code_title)
                                }
                                is GreenCardType.Eu -> {
                                    getString(R.string.my_overview_test_result_international_title)
                                }
                            }, data = QrCodeFragmentData(
                                shouldDisclose = cardItemUtil.shouldDisclose(cardItem),
                                credentials = credentials,
                                credentialExpirationTimeSeconds = expiration,
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
                buttonInfo = if (greenCardType == GreenCardType.Eu) {
                    ButtonInfo(
                        R.string.my_overview_description_eu_button_text,
                        R.string.my_overview_description_eu_button_link
                    )
                } else {
                    null
                },
            )
        )
    }
}
