/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.assertions

import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.end2end.actions.Overview.scrollToBottomOfOverview
import nl.rijksoverheid.ctr.holder.end2end.interaction.Barista.assertDisplayed
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.buttonIsEnabled
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.card
import nl.rijksoverheid.ctr.holder.end2end.interaction.Espresso.containsText
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilTextIsShown
import nl.rijksoverheid.ctr.holder.end2end.interaction.waitUntilViewIsShown
import nl.rijksoverheid.ctr.holder.end2end.model.Event
import nl.rijksoverheid.ctr.holder.end2end.model.EventType
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

    fun assertQrButtonIsEnabled(eventType: EventType, position: Int = 0) {
        card(eventType, position).buttonIsEnabled(qrButton, true)
    }

    fun assertQrButtonIsDisabled(eventType: EventType, position: Int = 0) {
        card(eventType, position).buttonIsEnabled(qrButton, false)
    }

    fun assertInternationalEventOnOverview(event: Event, position: Int = 0, dose: String? = null) {
        val card = card(event.eventType, position)

        scrollToBottomOfOverview(position)
        waitUntilViewIsShown(card)
        when (event) {
            is VaccinationEvent -> {
                card.containsText("Dosis $dose")
                card.containsText("Vaccinatiedatum: " + event.eventDate.written())
            }
            is PositiveTest -> {
                card.containsText("Geldig tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card.containsText("Type test: " + event.testType.value)
                card.containsText("Testdatum: " + event.eventDate.recently())
            }
        }
    }

    fun assertNotYetValidInternationalEventOnOverview(event: Event, position: Int = 0) {
        val card = card(event.eventType, position)

        scrollToBottomOfOverview(position)
        waitUntilViewIsShown(card(event.eventType, position))
        when (event) {
            is PositiveTest -> {
                card.containsText("geldig vanaf " + event.validFrom!!.writtenWithoutYear())
                card.containsText(" tot " + event.validUntil!!.written())
            }
            is NegativeTest -> {
                card.containsText("Type test: " + event.testType.value)
                card.containsText("geldig vanaf " + event.validFrom!!.written())
                card.containsText("Wordt automatisch geldig")
            }
        }
    }

    fun assertInternationalEventWillBecomeValid(eventType: EventType, position: Int = 0) {
        waitUntilViewIsShown(card(eventType, position))
        card(eventType, position).containsText("Wordt automatisch geldig")
    }

    fun assertInternationalEventWillExpireSoon(
        eventType: EventType,
        position: Int = 0,
        daysLeft: Int
    ) {
        waitUntilViewIsShown(card(eventType, position))
        card(eventType, position).containsText("Verloopt over $daysLeft dagen")
    }

    fun assertInternationalEventIsExpired(eventType: EventType) {
        waitUntilTextIsShown("Je internationale ${eventType.domesticName.lowercase()} is verlopen")
    }
}
