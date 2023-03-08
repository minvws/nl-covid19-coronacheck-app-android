package nl.rijksoverheid.ctr.appconfig.app_update

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaContentDescriptionAssertions.assertContentDescription
import com.adevinta.android.barista.interaction.BaristaViewPagerInteractions.swipeViewPagerForward
import nl.rijksoverheid.ctr.appconfig.models.AppUpdateData
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.appconfig.models.NewTerms
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class NewFeaturesFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
    }

    private fun featureItem() = NewFeatureItem(
        imageResource = R.drawable.illustration_new_disclosure_policy,
        titleResource = R.string.holder_newintheapp_content_3Gand1G_title,
        description = R.string.holder_newintheapp_content_3Gand1G_body,
        subTitleColor = R.color.link,
        subtitleResource = R.string.new_in_app_subtitle
    )

    private fun startFragment(): FragmentScenario<NewFeaturesFragment> {
        val fragmentArgs = bundleOf(
            "app_update_data" to AppUpdateData(
                newFeatures = listOf(
                    featureItem(), featureItem()
                ),
                newTerms = NewTerms(1, false),
                newFeatureVersion = 2,
                hideConsent = false
            )
        )
        return launchFragmentInContainer(
            fragmentArgs, themeResId = R.style.AppTheme
        ) {
            NewFeaturesFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }

    @Test
    fun `page indicators have correct content description`() {
        startFragment()

        assertContentDescription(R.id.indicators, "Pagina 1 van 2")

        swipeViewPagerForward()

        assertContentDescription(R.id.indicators, "Pagina 2 van 2")
    }
}
