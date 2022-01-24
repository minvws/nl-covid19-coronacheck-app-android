package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class CountryUtilImplTest {
    private val util = CountryUtilImpl()

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for the Netherlands in Dutch locale`() {
        val dutchString = util.getCountryForQrInfoScreen("NL", Locale("nl", "nl"))
        assertEquals("Nederland / The Netherlands", dutchString)
    }

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for Belgium in Dutch locale`() {
        val belgianString = util.getCountryForQrInfoScreen("be", Locale("nl", "nl"))
        assertEquals("België / Belgium", belgianString)
    }

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for the Netherlands in English locale`() {
        val dutchString = util.getCountryForQrInfoScreen("nl", Locale("en", "en"))
        assertEquals("Netherlands", dutchString)
    }
}