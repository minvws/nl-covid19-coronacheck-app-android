package nl.rijksoverheid.ctr.holder

import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.ActivityScenarioUtils.waitForActivity
import com.karumi.shot.ScreenshotTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleTest: ScreenshotTest {

    @Test
    fun createScreenShot() {
        val activity = startActivity()
        compareScreenshot(activity)
    }

    fun startActivity(args: Bundle = Bundle()): HolderMainActivity {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HolderMainActivity::class.java)
        intent.putExtras(args)
        val scenario = ActivityScenario.launch<HolderMainActivity>(intent)
        return scenario!!.waitForActivity()
    }
}