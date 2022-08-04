/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.get_events.utils

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.get_events.utils.ScopeUtilImpl
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
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
    fun `getScopeForRemoteOriginType returns empty when vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForRemoteOriginType(
            remoteOriginType = RemoteOriginType.Vaccination,
            getPositiveTestWithVaccination = false
        )
        assertEquals("", scope)
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
    fun `getScopeForOriginType returns empty when vaccination`() {
        val util = ScopeUtilImpl()
        val scope = util.getScopeForOriginType(
            originType = OriginType.Vaccination,
            getPositiveTestWithVaccination = false
        )
        assertEquals("", scope)
    }
}
