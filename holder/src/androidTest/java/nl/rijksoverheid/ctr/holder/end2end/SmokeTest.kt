package nl.rijksoverheid.ctr.holder.end2end

import nl.rijksoverheid.ctr.holder.end2end.utils.Elements.checkForText
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SmokeTest : BaseTest() {

    @Test
    fun start_app_and_see_overview() {
        checkForText("My certificates")
        checkForText("Menu")
    }

    @Test
    fun backend_password_is_available() {
        assertThat(authPassword, notNullValue())
    }
}
