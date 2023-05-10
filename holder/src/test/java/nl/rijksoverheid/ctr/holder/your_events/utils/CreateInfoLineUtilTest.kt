package nl.rijksoverheid.ctr.holder.your_events.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CreateInfoLineUtilTest : AutoCloseKoinTest() {

    private val createInfoLineUtil = object : CreateInfoLineUtil() {
    }

    @Test
    fun `given html string, when creating an info line, the html tags are stripped`() {
        val htmlString = "<a href=\"#\" id=\"click_me\">Test</ha>"

        val actual = createInfoLineUtil.createdLine("Title:", htmlString)

        assertEquals("Title: <b>Test</b><br/>", actual)
    }

    @Test
    fun `given html string, when creating an info line, the script tags are stripped`() {
        val htmlString = "<script type=\"text/javascript\">\n" +
                "   \$(function(){ \n" +
                "       console.log('malicious code'); \n" +
                "   });\n" +
                "</script>Test"

        val actual = createInfoLineUtil.createdLine("Title:", htmlString)

        assertEquals("Title: <b>\$(function(){ console.log('malicious code'); }); Test</b><br/>", actual)
    }
}
