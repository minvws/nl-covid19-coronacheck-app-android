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
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.YourEventFragmentEndState.*
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Test
import kotlin.test.assertEquals

class YourEventFragmentEndStateUtilImplTest {

    private val appConfigUseCase = mockk<CachedAppConfigUseCase>(relaxed = true)
    private val holderFeatureFlagUseCase = mockk<HolderFeatureFlagUseCase> {
        every { getDisclosurePolicy() } returns DisclosurePolicy.ThreeG
    }
    private val util = YourEventFragmentEndStateUtilImpl(appConfigUseCase, holderFeatureFlagUseCase)

    @Test
    fun `combination is not applicable when there is already a domestic vaccination stored`() {
        val storedGreenCards = listOf(
            fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Vaccination
            )
        )
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

        assertEquals(
            util.getResult(HolderFlow.Startup, storedGreenCards, events, remoteGreenCards),
            NotApplicable
        )
    }

    @Test
    fun `combination is only international vaccination`() {
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(origins = emptyList()),
            euGreencards = listOf(fakeEuGreenCard(origins = listOf(fakeOrigin(type = OriginType.Vaccination))))
        )

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), OnlyInternationalVaccination)
    }

    @Test
    fun `combination is only domestic vaccination`() {
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

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), OnlyDomesticVaccination(365))
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
            euGreencards = listOf()
        )

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), OnlyRecovery)
    }

    @Test
    fun `combination is vaccination and recovery`() {
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
            fakeEventGroupEntity(type = OriginType.Recovery)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(origins = listOf(fakeOrigin(type = OriginType.Recovery))),
            euGreencards = listOf(fakeEuGreenCard(origins = listOf(fakeOrigin(type = OriginType.Vaccination))))
        )

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), VaccinationAndRecovery)
    }

    @Test
    fun `combination is combined vaccination and recovery`() {
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

        assertEquals(
            util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards),
            CombinedVaccinationRecovery(365)
        )
    }

    @Test
    fun `combination is not applicable`() {
        every { appConfigUseCase.getCachedAppConfig().recoveryEventValidityDays } returns 365
        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination)
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(
                origins = listOf(fakeOrigin(type = OriginType.Vaccination))
            ),
            euGreencards = listOf(fakeEuGreenCard())
        )

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), NotApplicable)
    }

    @Test
    fun `combination is no positive test with stored vaccination`() {
        val storedGreenCards = listOf(
            fakeGreenCard(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Vaccination
            )
        )
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

        assertEquals(
            util.getResult(HolderFlow.Recovery, storedGreenCards, events, remoteGreenCards),
            NoRecoveryWithStoredVaccination
        )
    }

    @Test
    fun `on 0G the international only end state is not applicable`() {
        every { holderFeatureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ZeroG

        val events = listOf(
            fakeEventGroupEntity(type = OriginType.Vaccination),
        )
        val remoteGreenCards = fakeRemoteGreenCards(
            domesticGreencard = fakeDomesticGreenCard(origins = emptyList()),
            euGreencards = listOf(fakeEuGreenCard(origins = listOf(fakeOrigin(type = OriginType.Vaccination))))
        )

        assertEquals(util.getResult(HolderFlow.Startup, emptyList(), events, remoteGreenCards), NotApplicable)
    }
}