package nl.rijksoverheid.ctr.holder.end2end.utils

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import nl.rijksoverheid.ctr.holder.end2end.BaseTest

object Elements {

    @JvmStatic
    fun checkForText(text: String, timeout: Long = 5): Boolean {
        return BaseTest.device.wait(Until.hasObject(By.textContains(text)), timeout * 1000)!!
    }
}
