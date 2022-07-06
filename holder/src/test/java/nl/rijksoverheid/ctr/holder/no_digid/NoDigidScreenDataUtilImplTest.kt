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

        val firstButtonData = data.noDigidFragmentData.firstNavigationButtonData
        val secondButtonData = data.noDigidFragmentData.secondNavigationButtonData as NoDigidNavigationButtonData.Info

        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesHaveBSN, firstButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN), firstButtonData.subtitle)
        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN, secondButtonData.title)
        assertEquals(applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN), secondButtonData.subtitle)
        assertEquals(applicationContext.getString(R.string.holder_contactProviderHelpdesk_title, applicationContext.getString(R.string.holder_contactProviderHelpdesk_testLocation)), secondButtonData.infoFragmentData.title)
        assertEquals(R.string.holder_contactProviderHelpdesk_message, secondButtonData.infoFragmentData.descriptionData.htmlText)
        assertEquals(applicationContext.getString(R.string.general_toMyOverview), secondButtonData.infoFragmentData.primaryButtonData!!.text)
    }
}