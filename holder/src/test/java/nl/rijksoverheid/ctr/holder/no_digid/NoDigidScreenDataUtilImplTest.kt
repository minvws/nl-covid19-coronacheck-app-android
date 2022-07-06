package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoDigidScreenDataUtilImplTest : AutoCloseKoinTest() {
    private val applicationContext: Context by lazy { ApplicationProvider.getApplicationContext() }

    private val noDigidScreenDataUtil = NoDigidScreenDataUtilImpl(applicationContext)

    @Test
    fun `requestDigidButton has correct data`() {
        val data = noDigidScreenDataUtil.requestDigidButton()

        assertEquals(R.string.holder_noDigiD_buttonTitle_requestDigiD, data.title)
        assertEquals(R.drawable.ic_digid_logo, data.icon)
        assertEquals(applicationContext.getString(R.string.holder_noDigiD_url), data.externalUrl)
    }

    @Test
    fun `continueWithoutDigidButton has correct data`() {
        val data = noDigidScreenDataUtil.continueWithoutDigidButton(HolderFlow.Recovery)

        assertEquals(R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD, data.title)
        assertNull(data.icon)
        assertEquals(applicationContext.getString(R.string.holder_noDigiD_buttonSubTitle_continueWithoutDigiD), data.subtitle)

        val firstButtonData = data.noDigidFragmentData.firstNavigationButtonData as NoDigidNavigationButtonData.Info
        val secondButtonData = data.noDigidFragmentData.secondNavigationButtonData as NoDigidNavigationButtonData.NoDigid

        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesHaveBSN, firstButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN), firstButtonData.subtitle)
        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN, secondButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN), secondButtonData.subtitle)
        assertEquals(applicationContext.getString(R.string.holder_contactCoronaCheckHelpdesk_title), firstButtonData.infoFragmentData.title)
        assertEquals(R.string.holder_contactCoronaCheckHelpdesk_message, firstButtonData.infoFragmentData.descriptionData.htmlText)
        assertEquals(applicationContext.getString(R.string.holder_chooseEventLocation_title, applicationContext.getString(R.string.holder_contactProviderHelpdesk_tested)), secondButtonData.noDigidFragmentData.title)
        assertEquals("", secondButtonData.noDigidFragmentData.description)
        val eventLocationFirstButtonData = secondButtonData.noDigidFragmentData.firstNavigationButtonData as NoDigidNavigationButtonData.Info
        val eventLocationSecondButtonData = secondButtonData.noDigidFragmentData.secondNavigationButtonData as NoDigidNavigationButtonData.Info
        assertEquals(R.string.holder_chooseEventLocation_buttonTitle_GGD, eventLocationFirstButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_chooseEventLocation_buttonSubTitle_GGD), eventLocationFirstButtonData.subtitle)
        assertEquals(R.string.holder_chooseEventLocation_buttonTitle_other, eventLocationSecondButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_chooseEventLocation_buttonSubTitle_other), eventLocationSecondButtonData.subtitle)
        assertEquals(applicationContext.getString(R.string.holder_contactProviderHelpdesk_title), eventLocationSecondButtonData.infoFragmentData.title)
        assertEquals(R.string.holder_contactProviderHelpdesk_message, eventLocationSecondButtonData.infoFragmentData.descriptionData.htmlText)
        assertEquals(applicationContext.getString(R.string.general_toMyOverview), eventLocationSecondButtonData.infoFragmentData.primaryButtonData!!.text)
    }
}