package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import org.koin.androidx.viewmodel.ViewModelOwner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : Fragment(R.layout.fragment_my_overview) {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val EXTRA_BACK_FROM_QR = "EXTRA_BACK_FROM_QR"
        const val EXTRA_GREEN_CARD_TYPE = "GREEN_CARD_TYPE"
        const val EXTRA_RETURN_URI = "RETURN_URI"

        fun getInstance(
            greenCardType: GreenCardType,
            returnUri: String?
        ): MyOverviewFragment {
            val fragment = MyOverviewFragment()
            val arguments = Bundle()
            arguments.putParcelable(EXTRA_GREEN_CARD_TYPE, greenCardType)
            arguments.putString(EXTRA_RETURN_URI, returnUri)
            fragment.arguments = arguments
            return fragment
        }

    }

    private val dashboardViewModel: DashboardViewModel by sharedViewModelWithOwner(owner = { ViewModelOwner.from(requireParentFragment()) })
    private val section = Section()
    private val greenCardType: GreenCardType by lazy { arguments?.getParcelable<GreenCardType>(EXTRA_GREEN_CARD_TYPE) ?: error("EXTRA_GREEN_CARD_TYPE should not be null") }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        initRecyclerView(binding)
        observeItem()

        setFragmentResultListener(
            REQUEST_KEY
        ) { requestKey, bundle ->
            if (requestKey == REQUEST_KEY && bundle.getBoolean(
                    EXTRA_BACK_FROM_QR
                )
            ) {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun observeItem() {
        dashboardViewModel.dashboardTabItemsLiveData.observe(viewLifecycleOwner, {
            setItems(
                myDashboardItems = it.first { items -> items.greenCardType == greenCardType }.items
            )
        })
    }

    private fun initRecyclerView(binding: FragmentMyOverviewBinding) {
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
                is DashboardItem.HeaderItem -> {
                    adapterItems.add(
                        MyOverviewHeaderAdapterItem(
                            text = dashboardItem.text
                        )
                    )
                }
                is DashboardItem.PlaceholderCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardPlaceholderItem(
                            greenCardType = dashboardItem.greenCardType
                        )
                    )
                }
                is DashboardItem.GreenCardItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardAdapterItem(
                            greenCard = dashboardItem.greenCard,
                            originStates = dashboardItem.originStates,
                            credentialState = dashboardItem.credentialState,
                            databaseSyncerResult = dashboardItem.databaseSyncerResult,
                            onButtonClick = { greenCard, credential ->
                                navigateSafety(
                                    MyOverviewFragmentDirections.actionQrCode(
                                        toolbarTitle = when (greenCard.greenCardEntity.type) {
                                            is GreenCardType.Domestic -> {
                                                getString(R.string.domestic_qr_code_title)
                                            }
                                            is GreenCardType.Eu -> {
                                                getString(R.string.my_overview_test_result_international_title)
                                            }
                                        },
                                        data = QrCodeFragmentData(
                                            shouldDisclose = greenCard.greenCardEntity.type == GreenCardType.Domestic,
                                            credential = credential.data,
                                            credentialExpirationTimeSeconds = credential.expirationTime.toEpochSecond(),
                                            type = greenCard.greenCardEntity.type,
                                            originType = greenCard.origins.first().type
                                        ),
                                        returnUri = arguments?.getString(EXTRA_RETURN_URI)
                                    )
                                )
                            },
                            onRetryClick = {
                                dashboardViewModel.refresh(
                                    forceSync = true
                                )
                            },
                        )
                    )
                }
                is DashboardItem.GreenCardExpiredItem -> {
                    adapterItems.add(MyOverviewGreenCardExpiredAdapterItem(
                        greenCard = dashboardItem.greenCard,
                        onDismissClick = { item, greenCard ->
                            section.remove(item)
                            dashboardViewModel.removeGreenCard(greenCard)
                        }
                    ))
                }
                is DashboardItem.OriginInfoItem -> {
                    adapterItems.add(MyOverviewOriginInfoAdapterItem(
                        greenCardType = dashboardItem.greenCardType,
                        originType = dashboardItem.originType,
                        onInfoClick = { greenCardType, originType ->
                            when (greenCardType) {
                                is GreenCardType.Domestic -> navigateToDomesticQr(originType)
                                is GreenCardType.Eu -> navigateToEuQr(originType)
                            }
                        }
                    ))
                }
                is DashboardItem.ClockDeviationItem -> {
                    adapterItems.add(MyOverviewClockDeviationItem(onInfoIconClicked = {
                        navigateSafety(MyOverviewTabsFragmentDirections.actionShowClockDeviationExplanation())
                    }))
                }
                is DashboardItem.AddQrButtonItem -> {
                    (requireParentFragment() as MyOverviewTabsFragment).showAddQrButton(dashboardItem.show)
                }
            }
        }

        section.update(adapterItems)
    }

    private fun navigateToEuQr(originType: OriginType) {
        when (originType) {
            is OriginType.Test -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                    )
                )
            }
            is OriginType.Vaccination -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        description = getString(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_recovery)
                    )
                )
            }
        }
    }

    private fun navigateToDomesticQr(originType: OriginType) {
        when (originType) {
            is OriginType.Test -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        description = getString(
                            R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test
                        )
                    )
                )
            }
            is OriginType.Vaccination -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination)
                    )
                )
            }
            is OriginType.Recovery -> {
                navigateSafety(
                    MyOverviewTabsFragmentDirections.actionShowQrExplanation(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        description = getString(R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery)
                    )
                )
            }
        }
    }
}
