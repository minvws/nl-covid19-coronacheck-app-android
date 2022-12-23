package nl.rijksoverheid.ctr.holder.paper_proof

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.internal.assertAny
import com.adevinta.android.barista.internal.matcher.TextColorMatcher
import com.adevinta.android.barista.internal.matcher.withCompatText
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaperProofQrScannerFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_root)
        it.setCurrentDestination(R.id.nav_paper_proof_qr_scanner)
    }

    @Test
    fun `scanner toolbar title color is white`() {
        launchFragment()

        val toolbarTitle = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.add_paper_proof_qr_scanner_title)
        withCompatText(toolbarTitle).assertAny(TextColorMatcher(R.color.white))
    }

    private fun launchFragment() {
        launchFragmentInContainer(themeResId = R.style.AppTheme) {
            PaperProofQrScannerFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
