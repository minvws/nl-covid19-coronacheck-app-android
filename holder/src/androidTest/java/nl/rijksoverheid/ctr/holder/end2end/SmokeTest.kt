package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.utils.Actions.logVersions
import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertOverview
import org.junit.Test

class SmokeTest : BaseTest() {

    @Test
    fun startApp_logVersions() {
        assertOverview()
        logVersions()
    }
}
