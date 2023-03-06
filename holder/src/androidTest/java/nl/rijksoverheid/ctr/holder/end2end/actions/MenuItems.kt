/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickOn
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapButtonPosition
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown

object MenuItems {

    fun addEvent() {
        clickOn("Menu")
        clickOn("Vaccinatie of test toevoegen")
    }

    fun viewWallet() {
        clickOn("Menu")
        clickOn("Opgeslagen gegevens")
        waitUntilTextIsShown("Mijn opgeslagen gegevens")
    }

    fun deleteItemFromWallet(position: Int = 0, confirm: Boolean = true) {
        tapButtonPosition("Uit de app verwijderen", position)
        assertDisplayed("Deze gegevens verwijderen?")
        clickOn(if (confirm) "Verwijderen" else "Annuleer")
        waitUntilTextIsShown("Mijn opgeslagen gegevens")
    }

    fun returnFromWalletToOverview() {
        clickBack()
        clickBack()
    }
}
