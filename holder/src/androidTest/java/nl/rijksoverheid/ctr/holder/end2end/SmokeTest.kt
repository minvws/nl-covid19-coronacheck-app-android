/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.assertions.Overview.assertOverview
import org.junit.Test

class SmokeTest : BaseTest() {

    @Test
    fun startApp_logVersions() {
        assertOverview()
        logVersions()
    }
}
