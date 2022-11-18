package nl.rijksoverheid.ctr.holder.end2end

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import org.junit.Test

class SmokeTest : BaseTest() {

    @Test
    fun start_app_and_see_overview() {
        assertDisplayed("My certificates")
        assertDisplayed("Menu")
    }
}
