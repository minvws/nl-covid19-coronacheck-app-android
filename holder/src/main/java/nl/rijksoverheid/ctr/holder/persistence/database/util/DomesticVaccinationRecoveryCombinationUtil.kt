/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.util

import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.DomesticVaccinationRecoveryCombination
import nl.rijksoverheid.ctr.holder.persistence.database.models.DomesticVaccinationRecoveryCombination.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteGreenCards

interface DomesticVaccinationRecoveryCombinationUtil {

    fun getResult(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): DomesticVaccinationRecoveryCombination

    fun hasVaccinationAndRecoveryEvents(events: List<EventGroupEntity>): Boolean
}

class DomesticVaccinationRecoveryCombinationUtilImpl(
    private val appConfigUseCase: CachedAppConfigUseCase
) : DomesticVaccinationRecoveryCombinationUtil {

    override fun getResult(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): DomesticVaccinationRecoveryCombination {
        val recoveryValidityDays = appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays
        return when {
            isNoneWithoutRecovery(events, remoteGreenCards) -> NoneWithoutRecovery
            isOnlyVaccination(events, remoteGreenCards) -> OnlyVaccination(recoveryValidityDays)
            isOnlyRecovery(events, remoteGreenCards) -> OnlyRecovery
            isNoneWithRecovery(events, remoteGreenCards) -> NoneWithRecovery
            isCombinedVaccinationRecovery(events, remoteGreenCards) -> CombinedVaccinationRecovery(
                recoveryValidityDays
            )
            else -> NotApplicable
        }
    }

    private fun isNoneWithRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events)
                && hasOnlyInternationalVaccinationCertificates(remoteGreenCards)
    }

    private fun isOnlyRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Vaccination } ?: true &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Recovery } ?: false
    }

    private fun isOnlyVaccination(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return hasVaccinationAndRecoveryEvents(events) &&
                remoteGreenCards.domesticGreencard?.origins?.any { it.type == OriginType.Vaccination } ?: false &&
                remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Recovery } ?: true
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

    private fun isNoneWithoutRecovery(
        events: List<EventGroupEntity>,
        remoteGreenCards: RemoteGreenCards
    ): Boolean {
        return events.all { it.type == OriginType.Vaccination } &&
                hasOnlyInternationalVaccinationCertificates(remoteGreenCards)
    }

    private fun hasOnlyInternationalVaccinationCertificates(remoteGreenCards: RemoteGreenCards) =
        (remoteGreenCards.domesticGreencard?.origins?.none { it.type == OriginType.Vaccination } ?: true &&
                remoteGreenCards.euGreencards?.any { greenCard -> greenCard.origins.any { it.type == OriginType.Vaccination } } == true)
}