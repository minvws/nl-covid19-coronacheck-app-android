package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.utils.Assertions.assertOverview
import org.junit.Test

class SmokeTest : BaseTest() {

    @Test
    fun start_app_and_see_overview() {
        assertOverview()
    }
}
