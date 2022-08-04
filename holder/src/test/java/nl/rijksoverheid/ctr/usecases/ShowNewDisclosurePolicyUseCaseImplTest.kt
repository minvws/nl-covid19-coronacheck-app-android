/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.usecases

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecases.ShowNewDisclosurePolicyUseCaseImpl
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Test

class ShowNewDisclosurePolicyUseCaseImplTest {

    private val featureFlagUseCase: HolderFeatureFlagUseCase = mockk()
    private val persistenceManager: PersistenceManager = mockk(relaxed = true)

    private val newDisclosurePolicySeen = ShowNewDisclosurePolicyUseCaseImpl(
        featureFlagUseCase, persistenceManager
    )

    @Test
    fun `new policy should show when the disclosure policy differs from the one seen`() {
        every { featureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.OneG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        assertEquals(DisclosurePolicy.OneG, newDisclosurePolicySeen.get())
    }

    @Test
    fun `new policy should not show when the disclosure policy is the same as the one seen`() {
        every { featureFlagUseCase.getDisclosurePolicy() } returns DisclosurePolicy.ThreeG
        every { persistenceManager.getPolicyScreenSeen() } returns DisclosurePolicy.ThreeG

        assertNull(newDisclosurePolicySeen.get())
    }
}
