package nl.rijksoverheid.ctr.holder.no_digid

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoDigidScreenDataUtilImplTest : AutoCloseKoinTest() {
    private val applicationContext: Context by lazy { ApplicationProvider.getApplicationContext() }

    private val noDigidScreenDataUtil = NoDigidScreenDataUtilImpl(
        applicationContext,
        mockk<HolderFeatureFlagUseCase>().apply {
            every { getPapEnabled() } returns true
        },
        mockk<CachedAppConfigUseCase>().apply {
            every { getCachedAppConfig().contactInfo } returns AppConfig.ContactInformation(
                phoneNumber = "757961",
                phoneNumberAbroad = "+3112345678",
                startDay = 1,
                startHour = "12:00",
                endDay = 7,
                endHour = "18:00"
            )
        }
    )

    @Test
    fun `requestDigidButton has correct data`() {
        val data = noDigidScreenDataUtil.requestDigidButton()

        assertEquals(R.string.holder_noDigiD_buttonTitle_requestDigiD, data.title)
        assertEquals(R.drawable.ic_digid_logo, data.icon)
        assertEquals(applicationContext.getString(R.string.holder_noDigiD_url), data.externalUrl)
    }

    @Test
    fun `continueWithoutDigidButton has correct data`() {
        val data = noDigidScreenDataUtil.continueWithoutDigidButton(RemoteOriginType.Recovery)

        assertEquals(R.string.holder_noDigiD_buttonTitle_continueWithoutDigiD, data.title)
        assertNull(data.icon)
        assertEquals(
            applicationContext.getString(R.string.holder_noDigiD_buttonSubTitle_continueWithoutDigiD),
            data.subtitle
        )

        val firstButtonData =
            data.noDigidFragmentData.firstNavigationButtonData as NoDigidNavigationButtonData.Info
        val secondButtonData =
            data.noDigidFragmentData.secondNavigationButtonData as NoDigidNavigationButtonData.Ggd

        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesHaveBSN, firstButtonData.title)
        assertEquals(
            applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesHaveBSN),
            firstButtonData.subtitle
        )
        assertEquals(R.string.holder_checkForBSN_buttonTitle_doesNotHaveBSN, secondButtonData.title)
        assertEquals(
            applicationContext.getString(R.string.holder_checkForBSN_buttonSubTitle_doesNotHaveBSN_testFlow),
            secondButtonData.subtitle
        )
        assertEquals(
            applicationContext.getString(R.string.holder_contactCoronaCheckHelpdesk_title),
            firstButtonData.infoFragmentData.title
        )
        assertEquals(
            applicationContext.getString(
                R.string.holder_contactCoronaCheckHelpdesk_message,
                "every day",
                "12:00",
                "18:00",
                "757961",
                "757961",
                "+3112345678",
                "+3112345678"
            ),
            firstButtonData.infoFragmentData.descriptionData.htmlTextString
        )
    }
}
