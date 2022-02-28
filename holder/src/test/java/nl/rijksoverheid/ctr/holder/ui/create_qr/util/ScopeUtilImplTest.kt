/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteOriginType
import org.junit.Assert.assertEquals
import org.junit.Test

class ScopeUtilImplTest {

    @Test
    fun `getScopeForRemoteOriginType returns firstepisode when recovery and with incomplete vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForRemoteOriginType(
            remoteOriginType = RemoteOriginType.Recovery,
            getPositiveTestWithVaccination = true
        )
        assertEquals("firstepisode", scope)
    }

    @Test
    fun `getScopeForRemoteOriginType returns recovery when recovery and without incomplete vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForRemoteOriginType(
            remoteOriginType = RemoteOriginType.Recovery,
            getPositiveTestWithVaccination = false
        )
        assertEquals("recovery", scope)
    }

    @Test
    fun `getScopeForRemoteOriginType returns null when vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForRemoteOriginType(
            remoteOriginType = RemoteOriginType.Vaccination,
            getPositiveTestWithVaccination = false
        )
        assertEquals(null, scope)
    }

    @Test
    fun `getScopeForOriginType returns firstepisode when recovery and with incomplete vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForOriginType(
            originType = OriginType.Recovery,
            getPositiveTestWithVaccination = true
        )
        assertEquals("firstepisode", scope)
    }

    @Test
    fun `getScopeForOriginType returns recovery when recovery and with incomplete vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForOriginType(
            originType = OriginType.Recovery,
            getPositiveTestWithVaccination = false
        )
        assertEquals("recovery", scope)
    }

    @Test
    fun `getScopeForOriginType returns null when vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForOriginType(
            originType = OriginType.Vaccination,
            getPositiveTestWithVaccination = false
        )
        assertEquals(null, scope)
    }
}