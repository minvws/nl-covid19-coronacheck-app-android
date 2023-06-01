package nl.rijksoverheid.ctr.holder.data_migration

import kotlin.test.assertEquals
import org.junit.Test

class ProgressBarStateTests {
    @Test
    fun calculateProgressPercentage() {
        val progressBarState = ProgressBarState(
            max = 17,
            progress = 15
        )

        assertEquals(88, progressBarState.calculateProgressPercentage())
    }
}
