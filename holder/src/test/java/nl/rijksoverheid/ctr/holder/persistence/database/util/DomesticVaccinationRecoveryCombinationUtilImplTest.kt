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

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.*
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.DomesticVaccinationRecoveryCombination.*
import org.junit.Test
import kotlin.test.assertEquals

class DomesticVaccinationRecoveryCombinationUtilImplTest {

    private val appConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
    private val util = DomesticVaccinationRecoveryCombinationUtilImpl(appConfigUseCase)


    @Test
    fun `combination is none without recovery`() {
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(origins = emptyList()),
            euGreencards = listOf(fakeEuGreenCard(origins = listOf(fakeOrigin(type = OriginType.Vaccination))))
        )

        assertEquals(util.getResult(events, remoteGreenCards), NoneWithoutRecovery)
    }

    @Test
    fun `combination is only vaccination`() {
        every { appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays } returns 365
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
            fakeEventGroupEntity(type = OriginType.Recovery)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(
                origins = listOf(
                    fakeOrigin(type = OriginType.Vaccination)
                )
            ),
            euGreencards = listOf(fakeEuGreenCard())
        )

        assertEquals(util.getResult(events, remoteGreenCards), OnlyVaccination(365))
    }

    @Test
    fun `combination is only recovery`() {
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
            fakeEventGroupEntity(type = OriginType.Recovery)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(
                origins = listOf(
                    fakeOrigin(type = OriginType.Recovery)
                )
            ),
            euGreencards = listOf(fakeEuGreenCard())
        )

        assertEquals(util.getResult(events, remoteGreenCards), OnlyRecovery)
    }

    @Test
    fun `combination is none with recovery`() {
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
            fakeEventGroupEntity(type = OriginType.Recovery)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(origins = emptyList()),
            euGreencards = listOf(fakeEuGreenCard(origins = listOf(fakeOrigin(type = OriginType.Vaccination))))
        )

        assertEquals(util.getResult(events, remoteGreenCards), NoneWithRecovery)
    }

    @Test
    fun `combination is vaccination and recovery`() {
        every { appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays } returns 365
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
            fakeEventGroupEntity(type = OriginType.Recovery)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(
                origins = listOf(
                    fakeOrigin(type = OriginType.Vaccination),
                    fakeOrigin(type = OriginType.Recovery)
                )
            ),
            euGreencards = listOf(fakeEuGreenCard())
        )

        assertEquals(util.getResult(events, remoteGreenCards), CombinedVaccinationRecovery(365))
    }

    @Test
    fun `combination is not applicable`() {
        every { appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays } returns 365
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(
                origins = listOf(fakeOrigin(type = OriginType.Vaccination),)
            ),
            euGreencards = listOf(fakeEuGreenCard())
        )

        assertEquals(util.getResult(events, remoteGreenCards), NotApplicable)
    }
}