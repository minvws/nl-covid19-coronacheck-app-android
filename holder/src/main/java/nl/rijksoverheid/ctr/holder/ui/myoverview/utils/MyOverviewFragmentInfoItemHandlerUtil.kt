package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.design.utils.BottomSheetDialogUtil
import nl.rijksoverheid.ctr.design.utils.DescriptionData
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragment
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragmentDirections
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewInfoCardItem
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/**
 * Handles [MyOverviewInfoCardItem] actions
 */
interface MyOverviewFragmentInfoItemHandlerUtil {
    fun handleButtonClick(
        myOverviewFragment: MyOverviewFragment,
        infoItem: DashboardItem.InfoItem
    )

    fun handleDismiss(
        myOverviewFragment: MyOverviewFragment,
        infoCardItem: MyOverviewInfoCardItem,
        infoItem: DashboardItem.InfoItem.Dismissible
    )
}

class MyOverviewFragmentInfoItemHandlerUtilImpl(
    private val bottomSheetDialogUtil: BottomSheetDialogUtil
) : MyOverviewFragmentInfoItemHandlerUtil {

    /**
     * Handles the button click in the info card
     */
    override fun handleButtonClick(
        myOverviewFragment: MyOverviewFragment,
        infoItem: DashboardItem.InfoItem
    ) {
        when (infoItem) {
            is DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery -> {
                bottomSheetDialogUtil.present(
                    myOverviewFragment.childFragmentManager,
                    BottomSheetData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.extended_domestic_recovery_green_card_bottomsheet_title),
                        descriptionData = DescriptionData(R.string.extended_domestic_recovery_green_card_bottomsheet_description, htmlLinksEnabled = true),
                    )
                )
            }
            is DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery -> {
                bottomSheetDialogUtil.present(
                    myOverviewFragment.childFragmentManager,
                    BottomSheetData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.recovered_domestic_recovery_green_card_bottomsheet_title),
                        descriptionData = DescriptionData(R.string.recovered_domestic_recovery_green_card_bottomsheet_description),
                    )
                )
            }
            is DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations -> {
                bottomSheetDialogUtil.present(
                    myOverviewFragment.childFragmentManager,
                    BottomSheetData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.refreshed_eu_items_title),
                        descriptionData = DescriptionData(R.string.refreshed_eu_items_description, htmlLinksEnabled = true),
                    )
                )
            }
            is DashboardItem.InfoItem.NonDismissible.ExtendDomesticRecovery -> {
                myOverviewFragment.navigateSafety(
                    MyOverviewFragmentDirections.actionSyncGreenCards(
                        toolbarTitle = myOverviewFragment.getString(R.string.extend_domestic_recovery_green_card_toolbar_title),
                        title = myOverviewFragment.getString(R.string.extend_domestic_recovery_green_card_title),
                        description = myOverviewFragment.getString(R.string.extend_domestic_recovery_green_card_description),
                        button = myOverviewFragment.getString(R.string.extend_domestic_recovery_green_card_button)
                    )
                )
            }
            is DashboardItem.InfoItem.NonDismissible.RecoverDomesticRecovery -> {
                myOverviewFragment.navigateSafety(
                    MyOverviewFragmentDirections.actionSyncGreenCards(
                        toolbarTitle = myOverviewFragment.getString(R.string.recover_domestic_recovery_green_card_toolbar_title),
                        title = myOverviewFragment.getString(R.string.recover_domestic_recovery_green_card_title),
                        description = myOverviewFragment.getString(R.string.recover_domestic_recovery_green_card_description),
                        button = myOverviewFragment.getString(R.string.recover_domestic_recovery_green_card_button)
                    )
                )
            }
            is DashboardItem.InfoItem.NonDismissible.RefreshEuVaccinations -> {
                myOverviewFragment.navigateSafety(
                    MyOverviewFragmentDirections.actionSyncGreenCards(
                        toolbarTitle = myOverviewFragment.getString(R.string.refresh_eu_items_button),
                        title = myOverviewFragment.getString(R.string.refresh_eu_items_title),
                        description = myOverviewFragment.getString(R.string.refresh_eu_items_description),
                        button = myOverviewFragment.getString(R.string.refresh_eu_items_button)
                    )
                )
            }
            is DashboardItem.InfoItem.NonDismissible.ConfigFreshnessWarning -> {
                bottomSheetDialogUtil.present(
                    myOverviewFragment.childFragmentManager,
                    BottomSheetData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.config_warning_page_title),
                        descriptionData = DescriptionData(htmlTextString = myOverviewFragment.getString(
                            R.string.config_warning_page_message,
                            OffsetDateTime.ofInstant(
                                Instant.ofEpochSecond(infoItem.maxValidityDate),
                                ZoneOffset.UTC
                            ).formatDayMonthTime(myOverviewFragment.requireContext())
                        ),
                        htmlLinksEnabled = true)
                    )
                )

            }
        }
    }

    /**
     * Handles the dismiss button click in the info card
     */
    override fun handleDismiss(
        myOverviewFragment: MyOverviewFragment,
        infoCardItem: MyOverviewInfoCardItem,
        infoItem: DashboardItem.InfoItem.Dismissible
    ) {
        // Remove section from adapter
        myOverviewFragment.section.remove(infoCardItem)

        // Clear preference so it doesn't show again
        when (infoItem) {
            is DashboardItem.InfoItem.Dismissible.RefreshedEuVaccinations -> {
                myOverviewFragment.dashboardViewModel.dismissRefreshedEuVaccinationsInfoCard()
            }
            is DashboardItem.InfoItem.Dismissible.RecoveredDomesticRecovery -> {
                myOverviewFragment.dashboardViewModel.dismissRecoveredDomesticRecoveryInfoCard()
            }
            is DashboardItem.InfoItem.Dismissible.ExtendedDomesticRecovery -> {
                myOverviewFragment.dashboardViewModel.dismissExtendedDomesticRecoveryInfoCard()
            }
        }
    }
}