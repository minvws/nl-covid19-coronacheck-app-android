/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.assertions

import com.adevinta.android.barista.assertion.BaristaListAssertions.assertListItemCount
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.assertions.Retrieval.assertDetails
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.clickBack
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.tapWalletEvent
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.Person

object MenuItems {

    fun assertWalletItem(person: Person, event: Event) {
        tapWalletEvent(event)
        assertDetails(person, event)
        clickBack()
    }

    fun assertAmountOfWalletItemsPerSection(expectedAmount: Array<Int>) {
        // Header is 1, a section has a header and footer, and the events of the sections
        val amount = 1 + 2 * expectedAmount.size + expectedAmount.sum()
        assertListItemCount(R.id.savedEventsRecyclerView, amount)
    }

    fun assertNoEventsInWallet() {
        assertListItemCount(R.id.savedEventsRecyclerView, 2) // Header is 1, empty message is 1
    }
}
