package nl.rijksoverheid.ctr.introduction.privacy_consent

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.internal.performActionOnView
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyConsentFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `policy items are displayed in the fragment`() {
        startFragment(
            IntroductionData(
                privacyPolicyItems = listOf(
                    PrivacyPolicyItem(
                        R.drawable.shield,
                        R.string.onboarding_screen_1_description
                    ),
                    PrivacyPolicyItem(
                        R.drawable.shield,
                        R.string.onboarding_screen_3_description
                    )
                )
            )
        )

        assertDisplayed(R.string.onboarding_screen_1_description)
        assertDisplayed(R.string.onboarding_screen_3_description)
    }

    @Test
    fun `consent checkbox is hidden when hide consent is true`() {
        startFragment(
            IntroductionData(
                hideConsent = true
            )
        )

        assertNotDisplayed(R.id.checkbox_container)
    }

    @Test
    fun `consent checkbox is shown when hide consent is false`() {
        startFragment(
            IntroductionData(
                hideConsent = false
            )
        )

        assertDisplayed(R.id.checkbox_container)
    }

    @Test
    fun `when consent is not checked show error`() {
        startFragment(
            IntroductionData(
                hideConsent = false
            )
        )

        assertNotDisplayed(R.id.error_container)

        performActionOnView(ViewMatchers.withId(R.id.checkbox_button), ViewActions.click())

       assertDisplayed(R.id.error_container)
    }

    private fun startFragment(introductionData: IntroductionData): FragmentScenario<PrivacyConsentFragment> {
        return launchFragmentInContainer(
            bundleOf("introduction_data" to introductionData),
            themeResId = R.style.AppTheme
        ) {
            PrivacyConsentFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}