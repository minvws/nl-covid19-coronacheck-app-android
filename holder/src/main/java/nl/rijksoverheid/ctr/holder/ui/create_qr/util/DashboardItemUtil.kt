package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.DashboardItem
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase

interface DashboardItemUtil {
    fun getHeaderItemText(emptyState: Boolean, greenCardType: GreenCardType): Int
    fun shouldShowClockDeviationItem(emptyState: Boolean, allGreenCards: List<GreenCard>): Boolean
    fun shouldShowPlaceholderItem(emptyState: Boolean): Boolean
    fun shouldAddQrButtonItem(emptyState: Boolean): Boolean
    fun isAppUpdateAvailable(): Boolean

    /**
     * Multiple EU vaccination green card items will be combined into 1.
     *
     * @param[items] Items list containing possible multiple vaccination items to combine.
     * @return Items list with vaccination green card items combined into 1.
     */
    fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem>

    fun shouldShowExtendDomesticRecoveryItem(): Boolean
    fun shouldShowRecoverDomesticRecoveryItem(): Boolean
    fun shouldShowExtendedDomesticRecoveryItem(): Boolean
    fun shouldShowRecoveredDomesticRecoveryItem(): Boolean
    fun shouldShowConfigFreshnessWarning(): Boolean
    fun getConfigFreshnessMaxValidity(): Long
    fun shouldShowMissingDutchVaccinationItem(
        domesticGreenCards: List<GreenCard>,
        euGreenCards: List<GreenCard>,
    ): Boolean
    fun shouldShowCoronaMelderItem(
        greenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult
    ): Boolean
    fun shouldShowNewValidityItem(): Boolean
    fun shouldShowTestCertificate3GValidityItem(
        domesticGreenCards: List<GreenCard>
    ): Boolean
    fun shouldShowVisitorPassIncompleteItem(
        events: List<EventGroupEntity>,
        domesticGreenCards: List<GreenCard>
    ): Boolean
    fun shouldShowBoosterItem(
        greenCards: List<GreenCard>
    ): Boolean
}

class DashboardItemUtilImpl(
    private val clockDeviationUseCase: ClockDeviationUseCase,
    private val persistenceManager: PersistenceManager,
    private val appConfigFreshnessUseCase: AppConfigFreshnessUseCase,
    private val featureFlagUseCase: FeatureFlagUseCase,
    private val appConfigUseCase: CachedAppConfigUseCase,
    private val buildConfigUseCase: BuildConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
) : DashboardItemUtil {

    override fun getHeaderItemText(
        emptyState: Boolean,
        greenCardType: GreenCardType): Int {

        return when (greenCardType) {
            is GreenCardType.Domestic -> {
                if (emptyState) {
                    R.string.my_overview_qr_placeholder_description
                } else {
                    R.string.my_overview_description
                }
            }
            is GreenCardType.Eu -> {
                if (emptyState) {
                    R.string.my_overview_qr_placeholder_description_eu
                } else {
                    R.string.my_overview_description_eu
                }
            }
        }
    }

    override fun shouldShowClockDeviationItem(emptyState: Boolean, allGreenCards: List<GreenCard>) =
        clockDeviationUseCase.hasDeviation() && (!emptyState)

    override fun shouldShowPlaceholderItem(
        emptyState: Boolean
    ) = emptyState

    override fun shouldAddQrButtonItem(emptyState: Boolean): Boolean = emptyState

    override fun isAppUpdateAvailable(): Boolean {
        return buildConfigUseCase.getVersionCode() < appConfigUseCase.getCachedAppConfig().recommendedVersion
    }

    override fun combineEuVaccinationItems(items: List<DashboardItem>): List<DashboardItem> {
        return items
            .groupBy { it::class }
            .map { itemTypeToItem ->
                if (itemTypeToItem.value.first() !is DashboardItem.CardsItem) {
                    itemTypeToItem.value
                } else {
                    itemTypeToItem.value
                        .groupBy { (it as DashboardItem.CardsItem).cards.first().greenCard.origins.first().type }
                        .map {
                            if (it.key == OriginType.Vaccination) {
                                listOf(
                                    DashboardItem.CardsItem(it.value.map { greenCardsItem ->
                                        (greenCardsItem as DashboardItem.CardsItem).cards
                                    }.flatten())
                                )
                            } else it.value
                        }.flatten()
                }
            }.flatten()
    }

    override fun shouldShowExtendDomesticRecoveryItem(): Boolean {
        return persistenceManager.getShowExtendDomesticRecoveryInfoCard()
    }

    override fun shouldShowRecoverDomesticRecoveryItem(): Boolean {
        return persistenceManager.getShowRecoverDomesticRecoveryInfoCard()
    }

    override fun shouldShowExtendedDomesticRecoveryItem(): Boolean {
        return !persistenceManager.getHasDismissedExtendedDomesticRecoveryInfoCard()
    }

    override fun shouldShowRecoveredDomesticRecoveryItem(): Boolean {
        return !persistenceManager.getHasDismissedRecoveredDomesticRecoveryInfoCard()
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        // return true if config is older than 10 days && less than 28 days
       return appConfigFreshnessUseCase.shouldShowConfigFreshnessWarning()
    }

    override fun getConfigFreshnessMaxValidity(): Long {
        return appConfigFreshnessUseCase.getAppConfigMaxValidityTimestamp()
    }

    override fun shouldShowMissingDutchVaccinationItem(
        domesticGreenCards: List<GreenCard>,
        euGreenCards: List<GreenCard>,
    ): Boolean {
        // if a user has a european vaccination certificate but not dutch one,
        // we inform him that he can get a dutch one by either retrieving a
        // second vaccination result or a positive test result
        return domesticGreenCards.none { it.origins.any { it.type == OriginType.Vaccination } }
                && euGreenCards.any { it.origins.any { it.type == OriginType.Vaccination } }
    }

    override fun shouldShowCoronaMelderItem(
        greenCards: List<GreenCard>,
        databaseSyncerResult: DatabaseSyncerResult
    ): Boolean {
        return greenCards.isNotEmpty() && !greenCards.all { greenCardUtil.isExpired(it) } && databaseSyncerResult is DatabaseSyncerResult.Success
    }

    override fun shouldShowNewValidityItem(): Boolean {
        return !persistenceManager.getHasDismissedNewValidityInfoCard()
                && appConfigUseCase.getCachedAppConfig().showNewValidityInfoCard
    }

    override fun shouldShowTestCertificate3GValidityItem(domesticGreenCards: List<GreenCard>): Boolean {
        val isFeatureEnabled = featureFlagUseCase.isVerificationPolicyEnabled()
        val has3GTest = domesticGreenCards.any { greenCard ->
            greenCard.origins.any { it.type == OriginType.Test }
                    && greenCard.credentialEntities.any { it.category == Mobilecore.VERIFICATION_POLICY_3G }
        }
        return isFeatureEnabled && has3GTest
    }

    override fun shouldShowVisitorPassIncompleteItem(
        events: List<EventGroupEntity>,
        domesticGreenCards: List<GreenCard>): Boolean {
        val hasVaccinationAssessmentEvent = events.map { it.type }.contains(OriginType.VaccinationAssessment)
        val hasVaccinationAssessmentOrigin = domesticGreenCards.map { it.origins.map { origin -> origin.type } }.flatten().contains(OriginType.VaccinationAssessment)
        return hasVaccinationAssessmentEvent && !hasVaccinationAssessmentOrigin
    }

    override fun shouldShowBoosterItem(
        greenCards: List<GreenCard>
    ): Boolean {
        val boosterItemNotDismissedYet = persistenceManager.getHasDismissedBoosterInfoCard() == 0L
        val hasVaccinationOrigin = greenCards.map { it.origins.map { origin -> origin.type } }.flatten().contains(OriginType.Vaccination)
        return boosterItemNotDismissedYet && hasVaccinationOrigin
    }
}