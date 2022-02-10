package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentDirections
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewFragment
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewTabsFragmentDirections
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewInfoCardItem
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.MainNavDirections
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
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
        infoItem: DashboardItem.InfoItem
    )
}

class MyOverviewFragmentInfoItemHandlerUtilImpl(
    private val infoFragmentUtil: InfoFragmentUtil,
    private val intentUtil: IntentUtil,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : MyOverviewFragmentInfoItemHandlerUtil {

    /**
     * Handles the button click in the info card
     */
    override fun handleButtonClick(
        myOverviewFragment: MyOverviewFragment,
        infoItem: DashboardItem.InfoItem
    ) {
        when (infoItem) {
            is DashboardItem.InfoItem.ConfigFreshnessWarning ->
                onConfigRefreshClicked(myOverviewFragment, infoItem)
            is DashboardItem.InfoItem.ClockDeviationItem ->
                onClockDeviationClicked(myOverviewFragment)
            is DashboardItem.InfoItem.OriginInfoItem ->
                onOriginInfoClicked(myOverviewFragment, infoItem)
            is DashboardItem.InfoItem.MissingDutchVaccinationItem ->
                onMissingDutchVaccinationItemClicked(myOverviewFragment)
            is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> {
                onDomesticVaccinationExpiredItemClicked(myOverviewFragment)
            }
            is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> {
                onDomesticVaccinationAssessmentExpiredClicked(myOverviewFragment)
            }
            is DashboardItem.InfoItem.AppUpdate -> openPlayStore(myOverviewFragment)
            is DashboardItem.InfoItem.NewValidityItem -> {
                onNewValidityInfoClicked(myOverviewFragment.requireContext())
            }
            is DashboardItem.InfoItem.TestCertificate3GValidity -> {
                onTestCertificate3GValidityClicked(myOverviewFragment)
            }
            is DashboardItem.InfoItem.VisitorPassIncompleteItem -> {
                onVisitorPassIncompleteClicked(myOverviewFragment)
            }
            is DashboardItem.InfoItem.BoosterItem -> {
                onBoosterItemClicked(myOverviewFragment)
            }
            is DashboardItem.InfoItem.DisclosurePolicyItem -> {
                onDisclosurePolicyItemClicked()
            }
        }
    }

    private fun onDisclosurePolicyItemClicked() {
        // TODO: Add url link. Ticket: #3574
    }

    private fun onBoosterItemClicked(myOverviewFragment: MyOverviewFragment) {
        myOverviewFragment.navigateSafety(MyOverviewTabsFragmentDirections.actionGetEvents(
            originType = RemoteOriginType.Vaccination,
            afterIncompleteVaccination = false,
            toolbarTitle = myOverviewFragment.getString(R.string.choose_provider_toolbar),
        ))
    }

    private fun onDomesticVaccinationExpiredItemClicked(
        myOverviewFragment: MyOverviewFragment,
    ) {
        val navigationDirection = MainNavDirections.actionGetEvents(
            toolbarTitle = myOverviewFragment.getString(R.string.get_vaccination_title),
            originType = RemoteOriginType.Vaccination
        )

        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            InfoFragmentData.TitleDescriptionWithButton(
                title = myOverviewFragment.getString(R.string.holder_expiredDomesticVaccinationModal_title),
                descriptionData = DescriptionData(
                    R.string.holder_expiredDomesticVaccinationModal_body,
                    htmlLinksEnabled = true
                ),
                secondaryButtonData = ButtonData.NavigationButton(
                    text = myOverviewFragment.getString(R.string.holder_expiredDomesticVaccinationModal_button_addBoosterVaccination),
                    navigationActionId = navigationDirection.actionId,
                    navigationArguments = navigationDirection.arguments
                )
            )
        )
    }

    private fun onDomesticVaccinationAssessmentExpiredClicked(
        myOverviewFragment: MyOverviewFragment,
    ) {
        val descriptionText = myOverviewFragment.getString(R.string.holder_dashboard_visitorpassexpired_body,
            cachedAppConfigUseCase.getCachedAppConfig().vaccinationAssessmentEventValidityDays)

        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            InfoFragmentData.TitleDescriptionWithButton(
                title = myOverviewFragment.getString(R.string.holder_dashboard_visitorpassexpired_title),
                descriptionData = DescriptionData(
                    htmlTextString = descriptionText,
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun openPlayStore(myOverviewFragment: MyOverviewFragment) {
        intentUtil.openPlayStore(myOverviewFragment.requireContext())
    }

    private fun onMissingDutchVaccinationItemClicked(myOverviewFragment: MyOverviewFragment) {
        myOverviewFragment.navigateSafety(MyOverviewTabsFragmentDirections.actionMissingDutchCertificate())
    }

    private fun onConfigRefreshClicked(
        myOverviewFragment: MyOverviewFragment,
        infoItem: DashboardItem.InfoItem.ConfigFreshnessWarning
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            InfoFragmentData.TitleDescription(
                title = myOverviewFragment.getString(R.string.config_warning_page_title),
                descriptionData = DescriptionData(
                    htmlTextString = myOverviewFragment.getString(
                        R.string.config_warning_page_message,
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochSecond(infoItem.maxValidityDate),
                            ZoneOffset.UTC
                        ).formatDayMonthTime(myOverviewFragment.requireContext())
                    ),
                    htmlLinksEnabled = true
                )
            )
        )
    }

    private fun onClockDeviationClicked(
        myOverviewFragment: MyOverviewFragment
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager, InfoFragmentData.TitleDescription(
                title = myOverviewFragment.getString(R.string.clock_deviation_explanation_title),
                descriptionData = DescriptionData(
                    R.string.clock_deviation_explanation_description,
                    customLinkIntent = Intent(Settings.ACTION_DATE_SETTINGS)
                ),
            )
        )
    }


    private fun onTestCertificate3GValidityClicked(myOverviewFragment: MyOverviewFragment) {
        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            InfoFragmentData.TitleDescription(
                title = myOverviewFragment.getString(R.string.holder_my_overview_3g_test_validity_bottom_sheet_title),
                descriptionData = DescriptionData(
                    R.string.holder_my_overview_3g_test_validity_bottom_sheet_body,
                    htmlLinksEnabled = true
                ),
            )
        )
    }

    private fun onVisitorPassIncompleteClicked(myOverviewFragment: MyOverviewFragment) {
        val navigationDirection = InfoFragmentDirections.actionCommercialTestInputToken()

        infoFragmentUtil.presentFullScreen(
            currentFragment = myOverviewFragment,
            toolbarTitle = myOverviewFragment.getString(R.string.holder_completecertificate_toolbar),
            data = InfoFragmentData.TitleDescriptionWithButton(
                title = myOverviewFragment.getString(R.string.holder_completecertificate_title),
                descriptionData = DescriptionData(
                    htmlText = R.string.holder_completecertificate_body,
                    htmlLinksEnabled = true,
                ),
                primaryButtonData = ButtonData.NavigationButton(
                    text = myOverviewFragment.getString(R.string.holder_completecertificate_button_fetchnegativetest),
                    navigationActionId = navigationDirection.actionId,
                    navigationArguments = navigationDirection.arguments
                )
            )
        )
    }

    private fun onOriginInfoClicked(
        myOverviewFragment: MyOverviewFragment,
        item: DashboardItem.InfoItem.OriginInfoItem
    ) {
        when (item.greenCardType) {
            is GreenCardType.Domestic -> presentOriginInfoForDomesticQr(
                item.originType, myOverviewFragment
            )
            is GreenCardType.Eu -> presentOriginInfoForEuQr(
                item.originType, myOverviewFragment
            )
        }
    }

    private fun onNewValidityInfoClicked(context: Context) {
        context.getString(R.string.holder_dashboard_newvaliditybanner_url).launchUrl(context)
    }

    private fun presentOriginInfoForEuQr(
        originType: OriginType,
        myOverviewFragment: MyOverviewFragment
    ) {
        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            data = when (originType) {
                is OriginType.Test -> {
                    InfoFragmentData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_test),
                        descriptionData = DescriptionData(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_test),
                    )
                }
                is OriginType.Vaccination -> {
                    InfoFragmentData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                        descriptionData = DescriptionData(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_vaccination),
                    )
                }
                is OriginType.Recovery -> {
                    InfoFragmentData.TitleDescription(
                        title = myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_recovery),
                        descriptionData = DescriptionData(R.string.my_overview_green_card_not_valid_eu_but_is_in_domestic_bottom_sheet_description_recovery),
                    )
                }
                is OriginType.VaccinationAssessment -> {
                    InfoFragmentData.TitleDescription(
                        title =  myOverviewFragment.getString(R.string.holder_notvalidinthisregionmodal_visitorpass_international_title),
                        descriptionData = DescriptionData(
                            htmlText = R.string.holder_notvalidinthisregionmodal_visitorpass_international_body,
                            htmlLinksEnabled = true),
                    )
                }
            }
        )
    }

    private fun presentOriginInfoForDomesticQr(
        originType: OriginType,
        myOverviewFragment: MyOverviewFragment
    ) {
        val (title, description) = when (originType) {
            OriginType.Test -> Pair(
                myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_test),
                R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_test
            )
            OriginType.Vaccination -> Pair(
                myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_vaccination),
                R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_vaccination
            )
            OriginType.Recovery -> Pair(
                myOverviewFragment.getString(R.string.my_overview_green_card_not_valid_title_recovery),
                R.string.my_overview_green_card_not_valid_domestic_but_is_in_eu_bottom_sheet_description_recovery
            )
            OriginType.VaccinationAssessment -> Pair(
                // Missing domestic visitor pass can never happen
                myOverviewFragment.getString(R.string.holder_notvalidinthisregionmodal_visitorpass_international_title),
                R.string.holder_notvalidinthisregionmodal_visitorpass_international_body
            )
        }
        infoFragmentUtil.presentAsBottomSheet(
            myOverviewFragment.childFragmentManager,
            InfoFragmentData.TitleDescription(
                title = title,
                descriptionData = DescriptionData(description, htmlLinksEnabled = true),
            )
        )
    }

    /**
     * Handles the dismiss button click in the info card
     */
    override fun handleDismiss(
        myOverviewFragment: MyOverviewFragment,
        infoCardItem: MyOverviewInfoCardItem,
        infoItem: DashboardItem.InfoItem
    ) {
        // Remove section from adapter
        myOverviewFragment.section.remove(infoCardItem)

        // Clear preference so it doesn't show again
        when (infoItem) {
            is DashboardItem.InfoItem.GreenCardExpiredItem -> {
                myOverviewFragment.dashboardViewModel.removeGreenCard(infoItem.greenCardEntity)
            }
            is DashboardItem.InfoItem.DomesticVaccinationExpiredItem -> {
                myOverviewFragment.dashboardViewModel.removeGreenCard(infoItem.greenCardEntity)
            }
            is DashboardItem.InfoItem.DomesticVaccinationAssessmentExpiredItem -> {
                myOverviewFragment.dashboardViewModel.removeGreenCard(infoItem.greenCardEntity)
            }
            is DashboardItem.InfoItem.ClockDeviationItem,
            is DashboardItem.InfoItem.ConfigFreshnessWarning,
            is DashboardItem.InfoItem.OriginInfoItem,
            is DashboardItem.InfoItem.AppUpdate,
            is DashboardItem.InfoItem.MissingDutchVaccinationItem,
            is DashboardItem.InfoItem.TestCertificate3GValidity,
            is DashboardItem.InfoItem.VisitorPassIncompleteItem,
            is DashboardItem.InfoItem.NewValidityItem -> {
                myOverviewFragment.dashboardViewModel.dismissNewValidityInfoCard()
            }
            is DashboardItem.InfoItem.BoosterItem -> {
                myOverviewFragment.dashboardViewModel.dismissBoosterInfoCard()
            }
            is DashboardItem.InfoItem.DisclosurePolicyItem -> {
                myOverviewFragment.dashboardViewModel.dismissPolicyInfo(infoItem.disclosurePolicy)
            }
        }
    }
}