/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.assertions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.scrollToBottomOfOverview
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.buttonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.card
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.containsText
import nl.rijksoverheid.ctr.holder.end2end.interaction.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.NegativeTest
import nl.rijksoverheid.ctr.holder.end2end.model.PositiveTest
import nl.rijksoverheid.ctr.holder.end2end.model.VaccinationEvent
import nl.rijksoverheid.ctr.holder.end2end.model.written
import nl.rijksoverheid.ctr.holder.end2end.utils.recently
import nl.rijksoverheid.ctr.holder.end2end.utils.writtenWithoutYear

object Overview {

    private const val qrButton = R.id.button

    fun assertOverview() {
        waitUntilTextIsShown("Mijn bewijzen", 15)
        assertDisplayed("Menu")
    }

    fun assertQrButtonIsEnabled(eventType: Event.Type) {
        card(eventType).buttonIsEnabled(qrButton, true)
    }

    fun assertQrButtonIsDisabled(eventType: Event.Type) {
        card(eventType).buttonIsEnabled(qrButton, false)
    }

    fun assertInternationalEventOnOverview(event: Event, dose: String? = null) {
        scrollToBottomOfOverview()
        waitUntilViewIsShown(card(event.type))
        when (event) {
            is VaccinationEvent -> {
                card(Event.Type.Vaccination).containsText("Dosis $dose")
                card(Event.Type.Vaccination).containsText("Vaccinatiedatum: " + event.eventDate.written())
            }
            is PositiveTest -> {
                card(Event.Type.PositiveTest).containsText("Geldig tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card(Event.Type.NegativeTest).containsText("Type test: " + event.testType.value)
                card(Event.Type.NegativeTest).containsText("Testdatum: " + event.eventDate.recently())
            }
        }
    }

    fun assertNotYetValidInternationalEventOnOverview(event: Event) {
        scrollToBottomOfOverview()
        waitUntilViewIsShown(card(event.type))
        when (event) {
            is PositiveTest -> {
                card(Event.Type.PositiveTest).containsText("geldig vanaf " + event.validFrom!!.writtenWithoutYear())
                card(Event.Type.PositiveTest).containsText(" tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card(Event.Type.NegativeTest).containsText("Type test: " + event.testType.value)
                card(Event.Type.NegativeTest).containsText("geldig vanaf " + event.validFrom!!.written())
                card(Event.Type.NegativeTest).containsText("Wordt automatisch geldig")
            }
        }
    }

    fun assertInternationalEventWillBecomeValid(eventType: Event.Type) {
        waitUntilViewIsShown(card(eventType))
        card(eventType).containsText("Wordt automatisch geldig")
    }

    fun assertInternationalEventWillExpireSoon(eventType: Event.Type, daysLeft: Int) {
        waitUntilViewIsShown(card(eventType))
        card(eventType).containsText("Verloopt over $daysLeft dagen")
    }

    fun assertInternationalEventIsExpired(eventType: Event.Type) {
        waitUntilTextIsShown("Je internationale ${eventType.domesticName.lowercase()} is verlopen")
    }
}
