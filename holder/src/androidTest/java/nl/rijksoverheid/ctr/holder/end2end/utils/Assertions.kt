package nl.rijksoverheid.ctr.holder.end2end.utils

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn

object Assertions {

    fun assertOverview() {
        assertDisplayed("Mijn bewijzen")
        assertDisplayed("Menu")
        clickOn("Internationaal")
    }
}
