/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.model

import java.time.LocalDate

abstract class Event(
    open val eventType: EventType,
    open val eventDate: LocalDate,
    open val country: Country,
    open val validFrom: LocalDate?,
    open val validUntil: LocalDate?,
    open val disease: String = "COVID-19",
    open val issuer: String
) {

    enum class Country(val domesticName: String, val internationalName: String) {
        NL("Nederland", "Nederland / The Netherlands"),
    }

    companion object {
        const val minVws = "Ministerie van VWS / Ministry of Health, Welfare and Sport"
    }
}

enum class EventType(val value: String, val domesticName: String, val internationalName: String) {
    Vaccination("Vaccinatie", "Vaccinatiebewijs", "Internationaal vaccinatiebewijs"),
    PositiveTest("Positieve test", "Herstelbewijs", "Internationaal herstelbewijs"),
    NegativeTest("Negatieve test", "Testbewijs", "Internationaal testbewijs")
}

data class VaccinationEvent(
    override val eventDate: LocalDate,
    val vaccine: VaccineType,
    override val country: Country = Country.NL,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val issuer: String = minVws
) : Event(
    eventType = EventType.Vaccination,
    eventDate = eventDate,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    issuer = issuer
)

enum class VaccineType(val value: String, val type: String, val manufacturer: String) {
    Pfizer("Comirnaty (Pfizer)", "SARS-CoV-2 mRNA vaccine", "Biontech Manufacturing GmbH"),
    Moderna("Spikevax (Moderna)", "SARS-CoV-2 mRNA vaccine", "Moderna Biotech Spain S.L."),
    Janssen("Jcovden (Janssen)", "covid-19 vaccines", "Janssen-Cilag International")
}

abstract class TestEvent(
    override val eventType: EventType,
    override val eventDate: LocalDate,
    open val testType: TestType,
    override val country: Country,
    override val validFrom: LocalDate?,
    override val validUntil: LocalDate?,
    override val issuer: String,
    open val testLocation: TestLocation,
    open val testName: String,
    open val testProducer: String
) : Event(
    eventType = eventType,
    eventDate = eventDate,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    issuer = issuer
) {

    enum class TestLocation(val realName: String, val detailsName: String) {
        GgdXl("GGD XL Amsterdam", "Facility approved by the State of The Netherlands"),
        YellowBanana("Yellow Banana Test Center", "Facility approved by the State of The Netherlands")
    }
}

enum class TestType(val value: String, val testName: String, val testManufacturer: String) {
    Pcr("PCR (NAAT)", "PCR Name", "PCR Manufacturer")
}

data class PositiveTest(
    override val eventDate: LocalDate,
    override val country: Country = Country.NL,
    override val testType: TestType,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val issuer: String = minVws,
    override val testLocation: TestLocation = TestLocation.GgdXl,
    override val testName: String = testType.testName,
    override val testProducer: String = testType.testManufacturer
) : TestEvent(
    eventType = EventType.PositiveTest,
    eventDate = eventDate,
    testType = testType,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    issuer = issuer,
    testLocation = testLocation,
    testName = testName,
    testProducer = testProducer
)

open class NegativeTest(
    override val eventDate: LocalDate,
    override val country: Country = Country.NL,
    override val testType: TestType,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val issuer: String = minVws,
    override val testLocation: TestLocation = TestLocation.GgdXl,
    override val testName: String = testType.testName,
    override val testProducer: String = testType.testManufacturer
) : TestEvent(
    eventType = EventType.NegativeTest,
    eventDate = eventDate,
    testType = testType,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    issuer = issuer,
    testLocation = testLocation,
    testName = testName,
    testProducer = testProducer
)

class NegativeToken(
    override val eventType: EventType = EventType.NegativeTest,
    override val eventDate: LocalDate,
    override val testType: TestType,
    override val country: Country = Country.NL,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val issuer: String = minVws,
    override val testLocation: TestLocation = TestLocation.YellowBanana,
    override val testName: String = "Yellow Banana",
    override val testProducer: String = "Yellow Banana Company",
    val couplingCode: String,
    val verificationCode: String? = null
) : NegativeTest(
    eventDate = eventDate,
    testType = testType,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    issuer = issuer,
    testLocation = testLocation,
    testName = testName,
    testProducer = testProducer
)
