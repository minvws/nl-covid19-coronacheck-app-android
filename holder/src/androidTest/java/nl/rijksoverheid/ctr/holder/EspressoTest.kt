package nl.rijksoverheid.ctr.holder

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EspressoTest {
    private val context: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }
}