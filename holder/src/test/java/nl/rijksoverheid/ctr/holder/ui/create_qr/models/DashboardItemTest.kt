/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.models

import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import org.junit.Assert.*
import org.junit.Test

class DashboardItemTest {

    @Test
    fun `Green card expired item has a read more button when it's a domestic vaccination`() {
        val domesticVaccinationGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )

        assertTrue(DashboardItem.InfoItem.GreenCardExpiredItem(domesticVaccinationGreenCard).hasButton)
    }

    @Test
    fun `Green card expired item has no read more button when it's not a domestic vaccination`() {
        val euRecoveryGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery
        )
        val domesticRecoveryGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Recovery
        )
        val euVaccinationGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Vaccination
        )
        val euTestGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Test
        )
        val domesticTestGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Test
        )


        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(euRecoveryGreenCard).hasButton)
        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(domesticRecoveryGreenCard).hasButton)
        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(euVaccinationGreenCard).hasButton)
        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(euTestGreenCard).hasButton)
        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(domesticTestGreenCard).hasButton)
    }
}