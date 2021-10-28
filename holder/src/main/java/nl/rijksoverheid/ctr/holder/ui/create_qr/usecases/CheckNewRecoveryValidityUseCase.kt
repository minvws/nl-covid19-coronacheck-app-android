package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Backend switched from recovery green card 180 days validity to 365 days validity
 * To let this known to the user so the user can upgrade we show some cards in the UI [nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewInfoCardItem]
 * This usecase checks some logic (once if conditions are met) to check if it needs to be shown
 */
interface CheckNewRecoveryValidityUseCase {
    suspend fun check()
}

class CheckNewRecoveryValidityUseCaseImpl(
    private val clock: Clock,
    private val removeExpiredEventsUseCase: RemoveExpiredEventsUseCase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val persistenceManager: PersistenceManager,
    private val holderDatabase: HolderDatabase,
    private val originUtil: OriginUtil): CheckNewRecoveryValidityUseCase {

    override suspend fun check() {
        val recoveryGreencardRevisedValidityLaunchDateString = cachedAppConfigUseCase.getCachedAppConfig().recoveryGreenCardRevisedValidityLaunchDate
        val recoveryGreencardRevisedValidityLaunchDate = OffsetDateTime.ofInstant(Instant.parse(recoveryGreencardRevisedValidityLaunchDateString), ZoneId.of("UTC"))

        // Only start checking if local flag is set to true and the launch date is after the current date
        if (persistenceManager.getShouldCheckRecoveryGreenCardRevisedValidity() && recoveryGreencardRevisedValidityLaunchDate.isAfter(
                OffsetDateTime.now(clock))) {

            val allEvents = holderDatabase.eventGroupDao().getAll()

            // Check if we need to clean up events
            removeExpiredEventsUseCase.execute(allEvents)

            // Check if we have a valid recovery event stored, if so it means we are eligible to upgrade our validity
            val hasRecoveryEvent = allEvents.any { it.type is OriginType.Recovery }

            // Get our domestic green card (which have multiple origins)
            val domesticGreenCard = holderDatabase
                .greenCardDao()
                .getAll()
                .firstOrNull { it.greenCardEntity.type is GreenCardType.Domestic }

            // Check if there is a non expired recovery proof in the domestic green card
            val hasValidDomesticRecoveryOrigin = originUtil.getOriginState(domesticGreenCard?.origins ?: listOf())
                .any { it !is OriginState.Expired && it.origin.type is OriginType.Recovery }


            if (hasRecoveryEvent) {
                if (hasValidDomesticRecoveryOrigin) {
                    // We already have a domestic green card with a recovery proof, so we can extend
                    persistenceManager.setShowExtendDomesticRecoveryInfoCard(true)
                } else {
                    // We do not have a domestic green card with a recovery proof, but do have a event stored so we can renew
                    persistenceManager.setShowRecoverDomesticRecoveryInfoCard(true)
                }
            }

            // Make sure this check only gets executed once
            persistenceManager.setShouldCheckRecoveryGreenCardRevisedValidity(false)
        }
    }
}