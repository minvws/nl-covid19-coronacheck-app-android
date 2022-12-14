package nl.rijksoverheid.ctr.utils

import java.util.Locale
import nl.rijksoverheid.ctr.holder.utils.CountryUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class CountryUtilImplTest {
    private val util = CountryUtilImpl()

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for the Netherlands in Dutch locale`() {
        val dutchString = util.getCountryForQrInfoScreen("NL", Locale("nl"))
        assertEquals("Nederland / The Netherlands", dutchString)
    }

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for Belgium in Dutch locale`() {
        val belgianString = util.getCountryForQrInfoScreen("BE", Locale("nl"))
        assertEquals("België / Belgium", belgianString)
    }

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for the Netherlands in English locale`() {
        val dutchString = util.getCountryForQrInfoScreen("NL", Locale("en"))
        assertEquals("Netherlands", dutchString)
    }

    @Test
    fun `getCountryForQrInfoScreen returns correct strings for no Country`() {
        val dutchString = util.getCountryForQrInfoScreen(null, Locale("nl"))
        assertEquals("", dutchString)
    }
}
