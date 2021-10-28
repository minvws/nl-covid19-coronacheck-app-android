package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.design.utils.BottomSheetDialogUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardSync
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeFragmentData
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
class MyOverviewFragment : Fragment(R.layout.fragment_my_overview) {

    companion object {
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

    private val bottomSheetDialogUtil: BottomSheetDialogUtil by inject()
    private val dashboardViewModel: DashboardViewModel by sharedViewModelWithOwner(owner = {
        ViewModelOwner.from(
            requireParentFragment()
        )
    })
    private val section = Section()
    private val greenCardType: GreenCardType by lazy {
        arguments?.getParcelable<GreenCardType>(
            EXTRA_GREEN_CARD_TYPE
        ) ?: error("EXTRA_GREEN_CARD_TYPE should not be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        initRecyclerView(binding)
        observeItem()
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
                            text = dashboardItem.text,
                            buttonInfo =  if (greenCardType == GreenCardType.Eu) {
                                ButtonInfo(R.string.my_overview_description_eu_button_text, R.string.my_overview_description_eu_button_link)
                            } else {
                                null
                            },
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
                is DashboardItem.CardsItem -> {
                    adapterItems.add(
                        MyOverviewGreenCardAdapterItem(
                            cards = dashboardItem.cards,
                            onButtonClick = { greenCard, credentials, expiration ->
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
                                            credentials = credentials,
                                            credentialExpirationTimeSeconds = expiration,
                                            type = greenCard.greenCardEntity.type,
                                            originType = greenCard.origins.first().type
                                        ),
                                        returnUri = arguments?.getString(EXTRA_RETURN_URI)
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
                                is GreenCardType.Domestic -> presentOriginInfoForDomesticQr(originType)
                                is GreenCardType.Eu -> presentOriginInfoForEuQr(originType)
                            }
                        }
                    ))
                }
                is DashboardItem.ClockDeviationItem -> {
                    adapterItems.add(MyOverviewClockDeviationItem(onInfoIconClicked = {
                        bottomSheetDialogUtil.present(
                            childFragmentManager, BottomSheetData.TitleDescription(
                                title = getString(R.string.clock_deviation_explanation_title),
                                applyOnDescription = {
                                    it.setHtmlText(R.string.clock_deviation_explanation_description)
                                    it.enableCustomLinks {
                                        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                                        startActivity(intent)
                                    }
                                },
                            )
                        )
                    }))
                }
                is DashboardItem.AddQrButtonItem -> {
                    (requireParentFragment() as MyOverviewTabsFragment).showAddQrButton(
                        dashboardItem.show
                    )
                }
                is DashboardItem.InfoItem -> {
                    adapterItems.add(MyOverviewInfoCardItem(
                        infoItem = dashboardItem,
                        onButtonClick = {
                            when (it) {
                                is DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery -> {
                                    bottomSheetDialogUtil.present(
                                        childFragmentManager,
                                        BottomSheetData.TitleDescription(
                                            title = getString(R.string.extended_domestic_recovery_green_card_bottomsheet_title),
                                            applyOnDescription = { htmlTextViewWidget ->
                                                htmlTextViewWidget.setHtmlText(getString(R.string.extended_domestic_recovery_green_card_bottomsheet_description), true)
                                            }
                                        )
                                    )
                                }
                                is DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery -> {
                                    bottomSheetDialogUtil.present(
                                        childFragmentManager,
                                        BottomSheetData.TitleDescription(
                                            title = getString(R.string.recovered_domestic_recovery_green_card_bottomsheet_title),
                                            applyOnDescription = { htmlTextViewWidget ->
                                                htmlTextViewWidget.setHtmlText(getString(R.string.recovered_domestic_recovery_green_card_bottomsheet_description), true)
                                            }
                                        )
                                    )
                                }
                                is DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations -> {
                                    bottomSheetDialogUtil.present(
                                        childFragmentManager,
                                        BottomSheetData.TitleDescription(
                                            title = getString(R.string.refreshed_eu_items_title),
                                            applyOnDescription = { htmlTextViewWidget ->
                                                htmlTextViewWidget.setHtmlText(getString(R.string.refreshed_eu_items_description), true)
                                            }
                                        )
                                    )
                                }
                                is DashboardItem.InfoItem.NonDismissible.ExtendDomesticRecovery -> {
                                    navigateSafety(
                                        MyOverviewFragmentDirections.actionSyncGreenCards(
                                            title = getString(R.string.extend_domestic_recovery_green_card_title),
                                            description = getString(R.string.extend_domestic_recovery_green_card_description),
                                            button = getString(R.string.extend_domestic_recovery_green_card_button)
                                        )
                                    )
                                }
                                is DashboardItem.InfoItem.NonDismissible.RecoverDomesticRecovery -> {
                                    navigateSafety(
                                        MyOverviewFragmentDirections.actionSyncGreenCards(
                                            title = getString(R.string.recover_domestic_recovery_green_card_title),
                                            description = getString(R.string.recover_domestic_recovery_green_card_description),
                                            button = getString(R.string.recovery_domestic_recovery_green_card_button)
                                        )
                                    )
                                }
                                is DashboardItem.InfoItem.NonDismissible.RefreshEuVaccinations -> {
                                    navigateSafety(
                                        MyOverviewFragmentDirections.actionSyncGreenCards(
                                            title = getString(R.string.refresh_eu_items_title),
                                            description = getString(R.string.refresh_eu_items_description),
                                            button = getString(R.string.refresh_eu_items_button)
                                        )
                                    )
                                }
                            }
                        },
                        onDismiss = { infoCardItem, infoItem ->
                            // Remove section from adapter
                            section.remove(infoCardItem)

                            // Clear preference so it doesn't show again
                            when (infoItem) {
                                is DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations -> {
                                    dashboardViewModel.dismissRefreshedEuVaccinationsInfoCard()
                                }
                                is DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery -> {
                                    dashboardViewModel.dismissRecoveredDomesticRecoveryInfoCard()
                                }
                                is DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery -> {
                                    dashboardViewModel.dismissExtendedDomesticRecoveryInfoCard()
                                }
                            }
                        }
                    ))
                }
            }
        }

        section.update(adapterItems)
    }

    private fun presentOriginInfoForEuQr(originType: OriginType) {
        bottomSheetDialogUtil.present(childFragmentManager,
            data = when (originType) {
                is OriginType.Test -> {
                    BottomSheetData.TitleDescription(
                        title = getString(R.string.my_overview_green_card_not_valid_title_test),
                        applyOnDescription = {
                            it.setHtmlText(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test)
                        }
                    )
                }
                is OriginType.Vaccination -> {
                    BottomSheetData.TitleDescription(
                        title = getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        applyOnDescription = {
                            it.setHtmlText(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination)
                        }
                    )
                }
                is OriginType.Recovery -> {
                    BottomSheetData.TitleDescription(
                        title = getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        applyOnDescription = {
                            it.setHtmlText(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_recovery)
                        }
                    )
                }
            })
    }

    private fun presentOriginInfoForDomesticQr(originType: OriginType) {
        val (title, description) = when (originType) {
            OriginType.Test -> Pair(getString(R.string.my_overview_green_card_not_valid_title_test), R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test)
            OriginType.Vaccination -> Pair(getString(R.string.my_overview_green_card_not_valid_title_vaccination), R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination)
            OriginType.Recovery -> Pair(getString(R.string.my_overview_green_card_not_valid_title_recovery), R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery)
        }
        bottomSheetDialogUtil.present(childFragmentManager,
            BottomSheetData.TitleDescription(
                title = title,
                applyOnDescription = {
                    it.setHtmlText(description, true)
                }
            )
        )
    }
}
