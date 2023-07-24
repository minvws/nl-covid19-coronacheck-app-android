package nl.rijksoverheid.ctr.holder.pdf

import android.app.Instrumentation
import android.content.Intent
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.BundleMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.UriMatchers
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import nl.rijksoverheid.ctr.holder.R
import org.hamcrest.CoreMatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PdfExportedFragmentTest : AutoCloseKoinTest() {
    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_pdf_exported)
    }

    @Test
    fun `click on previewPdfButton shows pdf preview`() {
        launchFragment()

        clickOn(R.id.previewPdfButton)

        assertEquals(R.id.nav_pdf_preview, navController.currentDestination?.id)
    }

    @Test
    fun `click on save pdf button displays action chooser`() {
        Intents.init()
        val expectedIntent = CoreMatchers.allOf(
            IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
            IntentMatchers.hasExtras(
                CoreMatchers.allOf(
                    BundleMatchers.hasEntry(Intent.EXTRA_TITLE, CoreMatchers.equalTo("Save file")),
                    BundleMatchers.hasEntry(
                        Intent.EXTRA_INTENT, CoreMatchers.allOf(
                            IntentMatchers.hasAction(Intent.ACTION_SEND),
                            IntentMatchers.hasType("application/pdf"),
                            IntentMatchers.hasFlag(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                            IntentMatchers.hasExtra(
                                CoreMatchers.equalTo(Intent.EXTRA_STREAM),
                                UriMatchers.hasPath("/files/Coronacheck - International.pdf")
                            )
                        )
                    )
                )

            )
        )
        Intents.intending(expectedIntent).respondWith(Instrumentation.ActivityResult(0, null))
        launchFragment()

        clickOn(R.id.savePdfButton)

        Intents.intended(expectedIntent)

        Intents.release()
    }

    private fun launchFragment() {
        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            PdfExportedFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
