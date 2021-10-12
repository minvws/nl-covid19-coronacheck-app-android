package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class CountryUtilImplTest {
    private val util = CountryUtilImpl()

    @Test
    fun `getCountry returns correct strings for the Netherlands in Dutch locale`() {
        val dutchString = util.getCountry("NL", Locale("nl", "nl"))
        assertEquals("Nederland / The Netherlands", dutchString)
    }

    @Test
    fun `getCountry returns correct strings for Belgium in Dutch locale`() {
        val belgianString = util.getCountry("be", Locale("nl", "nl"))
        assertEquals("België / Belgium", belgianString)
    }

    @Test
    fun `getCountry returns correct strings for the Netherlands in English locale`() {
        val dutchString = util.getCountry("nl", Locale("en", "en"))
        assertEquals("Netherlands", dutchString)
    }
}