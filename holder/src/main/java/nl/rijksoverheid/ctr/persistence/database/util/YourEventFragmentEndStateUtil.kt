/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.persistence.database.util

import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.AddedNegativeTestInVaccinationAssessmentFlow
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.CombinedVaccinationRecovery
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.NoRecoveryWithStoredVaccination
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.NotApplicable
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.OnlyDomesticVaccination
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.OnlyInternationalVaccination
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.OnlyRecovery
import nl.rijksoverheid.ctr.persistence.database.models.YourEventFragmentEndState.VaccinationAndRecovery
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.Flow

interface YourEventFragmentEndStateUtil {

    fun getResult(
        flow: Flow,
        storedGreenCards: List<GreenCard>,
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): YourEventFragmentEndState

    fun hasVaccinationAndRecoveryEvents(events: List<EventGroupEntity>): Boolean
}

class YourEventFragmentEndStateUtilImpl(
    private val appConfigUseCase: HolderCachedAppConfigUseCase,
    private val holderFeatureFlagUseCase: HolderFeatureFlagUseCase
) : YourEventFragmentEndStateUtil {

    override fun getResult(
        flow: Flow,
        storedGreenCards: List<GreenCard>,
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): YourEventFragmentEndState {
        val recoveryValidityDays = appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays
        return when {
            hasAddedNegativeTestInVaccinationAssessmentFlow(flow, remoteGreenCards) -> AddedNegativeTestInVaccinationAssessmentFlow
            hasStoredDomesticVaccination(storedGreenCards) -> NotApplicable
            isNoRecoveryWithStoredVaccination(events, remoteGreenCards, storedGreenCards) -> NoRecoveryWithStoredVaccination
            isOnlyDomesticVaccination(events, remoteGreenCards) -> OnlyDomesticVaccination(recoveryValidityDays)
            isVaccinationAndRecovery(events, remoteGreenCards) -> VaccinationAndRecovery
            isOnlyInternationalVaccination(events, remoteGreenCards) -> OnlyInternationalVaccination
            isOnlyRecovery(events, remoteGreenCards) -> OnlyRecovery
            isCombinedVaccinationRecovery(events, remoteGreenCards) -> CombinedVaccinationRecovery(
                recoveryValidityDays
            )
            else -> NotApplicable
        }
    }

    /**
     * Check whether there is already domestic vaccination stored in the database. If there is one
     * there is no need to make a combination.
     *
     * @return whether the database contains a domestic green card with vaccination origin
     */
    private fun hasStoredDomesticVaccination(storedGreenCards: List<GreenCard>): Boolean {
        return storedGreenCards
            .filter { it.greenCardEntity.type == GreenCardType.Domestic }
            .any { greenCard ->
                greenCard.origins.any { it.type == OriginType.Vaccination }
            }
    }

    private fun isVaccinationAndRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                hasOnlyInternationalVaccinationCertificates(remoteGreenCards) &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Recovery } ?: false
    }

    private fun isOnlyRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Vaccination } ?: true &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Recovery } ?: false
    }

    private fun isOnlyDomesticVaccination(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Vaccination } ?: false &&
                remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Recovery } ?: true
    }

    private fun isNoRecoveryWithStoredVaccination(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards,
        storedGreenCards: List<GreenCard>
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Recovery } ?: true &&
                hasStoredDomesticVaccination(storedGreenCards)
    }

    private fun isCombinedVaccinationRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Vaccination } ?: false &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Recovery } ?: false
    }

    override fun hasVaccinationAndRecoveryEvents(events: List<EventGroupEntity>) =
        events.any { it.type == OriginType.Vaccination } && events.any { it.type == OriginType.Recovery }

    private fun isOnlyInternationalVaccination(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return (events.all { it.type == OriginType.Vaccination } || hasVaccinationAndRecoveryEvents(events)) &&
                hasOnlyInternationalVaccinationCertificates(remoteGreenCards) &&
                holderFeatureFlagUseCase.getDisclosurePolicy() != DisclosurePolicy.ZeroG
    }

    private fun hasOnlyInternationalVaccinationCertificates(remoteGreenCards: RemoteGreenCards) =
        (remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Vaccination } ?: true &&
                remoteGreenCards.euGreencards?.any { greenCard -> greenCard.origins.any { it.type == OriginType.Vaccination } } == true)

    private fun hasAddedNegativeTestInVaccinationAssessmentFlow(
        flow: Flow,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return if (flow == HolderFlow.VaccinationAssessment) {
            val hasTest = remoteGreenCards.getAllOrigins().any { it is OriginType.Test }
            val hasVisitorPass =
                remoteGreenCards.getAllOrigins().any { it is OriginType.VaccinationAssessment }
            return hasTest && !hasVisitorPass
        } else {
            false
        }
    }
}
