package nl.rijksoverheid.ctr.introduction.onboarding

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
import com.adevinta.android.barista.internal.performActionOnView
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OnboardingFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun `When onboarding is shown the first onboarding item is visible`() {
        startFragment(
            IntroductionData(
                listOf(
                    OnboardingItem(
                        R.drawable.illustration_onboarding_2,
                        R.string.onboarding_screen_1_title,
                        R.string.onboarding_screen_1_description
                    )
                )
            )
        )

        assertDisplayed(R.string.onboarding_screen_1_title)
        assertDisplayed(R.string.onboarding_screen_1_description)
    }

    @Test
    fun `When clicking on next the onboarding item is shown`() {
        startFragment(
            IntroductionData(
                listOf(
                    OnboardingItem(
                        R.drawable.illustration_onboarding_2,
                        R.string.onboarding_screen_2_title,
                        R.string.onboarding_screen_2_description
                    ),
                    OnboardingItem(
                        R.drawable.illustration_onboarding_4,
                        R.string.onboarding_screen_3_title,
                        R.string.onboarding_screen_3_description
                    )
                )
            )
        )

        performActionOnView(ViewMatchers.withId(R.id.button), ViewActions.click())

        assertDisplayed(R.string.onboarding_screen_3_title)
        assertDisplayed(R.string.onboarding_screen_3_description)
    }

    private fun startFragment(introductionData: IntroductionData): FragmentScenario<OnboardingFragment> {
        return launchFragmentInContainer(
            bundleOf("introduction_data" to introductionData),
            themeResId = R.style.AppTheme
        ) {
            OnboardingFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
