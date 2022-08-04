package nl.rijksoverheid.ctr.holder.paper_proof

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.holder.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaperProofDomesticInputCodeFragmentScreenshotTest : ScreenshotTest {
    @Test
    fun paperProofDomesticInputCodeFragment_Screenshot() {
        val fragmentScenario = launchFragmentInContainer<PaperProofDomesticInputCodeFragment>(
            bundleOf(
                "qrContent" to "HC1:NCFO20\$80T9WTWGVLK-49NJ3B0J\$OCC*AX*4FBB.R3*70J+9DN03E52F3%0US.3Y50.FK8ZKO/EZKEZ967L6C56GVC*JC1A6QW63W5KF6746TPCBEC7ZKW.CSEE*KEQPC.OEFOAF\$DN34VKE0/DLPCG/DSEE5IA\$M8NNASNAQY9 R7.HAB+9 JC:.DNUAU3EI3D5WE TAQ1A7:EDOL9WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46.JCP9EJY8L/5M/5546.96VF6.JCBECB1A-:8\$966469L6OF6VX6FVCPD0KQEPD0LVC6JD846Y96D463W5307UPCBJCOT9+EDL8FHZ95/D QEALEN44:+C%69AECAWE:34: CJ.CZKE9440/D+34S9E5LEWJC0FD3%4AIA%G7ZM81G72A6J+9QG7OIBENA.S90IAY+A17A+B9:CB*6AVX8AF6F:5678M2927SM6NAN24WKP0VTMO8.CMJF1CF-*7%XN3R0C0E45L0EKUGEA-SL0HYN71PBTWHCITDHPIHG/A7%8U9PEBHEPD9DD4\$O4000FGW5HIWGG"
            ),
            themeResId = R.style.TestAppTheme
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }
}
