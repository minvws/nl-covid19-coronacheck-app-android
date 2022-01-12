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
        val euRecoveryGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Eu,
            originType = OriginType.Recovery
        )
        val domesticVaccinationGreenCard = fakeGreenCard(
            greenCardType = GreenCardType.Domestic,
            originType = OriginType.Vaccination
        )

        assertFalse(DashboardItem.InfoItem.GreenCardExpiredItem(euRecoveryGreenCard).hasButton)
        assertTrue(DashboardItem.InfoItem.GreenCardExpiredItem(domesticVaccinationGreenCard).hasButton)
    }
}