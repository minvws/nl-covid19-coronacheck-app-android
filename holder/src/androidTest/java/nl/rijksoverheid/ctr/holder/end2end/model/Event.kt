/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.model

import java.time.LocalDate

abstract class Event(
    open val type: Type,
    open val eventDate: LocalDate,
    open val country: Country,
    open val validFrom: LocalDate?,
    open val validUntil: LocalDate?,
    open val disease: String = "COVID-19"
) {

    enum class Type(val value: String, val domesticName: String, val internationalName: String) {
        Vaccination("Vaccinatie", "Vaccinatiebewijs", "Internationaal vaccinatiebewijs"),
        PositiveTest("Positieve test", "Herstelbewijs", "Internationaal herstelbewijs"),
        NegativeTest("Negatieve test", "Testbewijs", "Internationaal testbewijs")
    }

    enum class Country(val domesticName: String, val internationalName: String) {
        NL("Nederland", "Nederland / The Netherlands"),
        DE("Duitsland", "Duitsland / Germany");
    }
}

data class Vaccination(
    override val eventDate: LocalDate,
    val vaccine: VaccineType,
    override val country: Country = Country.NL,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null
) : Event(
    type = Type.Vaccination,
    eventDate = eventDate,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil
) {

    enum class VaccineType(val value: String) {
        Pfizer("Comirnaty (Pfizer)"),
        Moderna("Spikevax (Moderna)"),
        Janssen("Jcovden (Janssen)")
    }
}

abstract class TestEvent(
    override val type: Type,
    override val eventDate: LocalDate,
    open val testType: TestType,
    override val country: Country = Country.NL,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    open val dcc: String? = null,
    open val couplingCode: String? = null
) : Event(
    type = type,
    eventDate = eventDate,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil
) {
    enum class TestType(val value: String) {
        Pcr("PCR (NAAT)"),
        Rat("Sneltest (RAT)")
    }
}

data class PositiveTest(
    override val eventDate: LocalDate,
    override val country: Country = Country.NL,
    override val testType: TestType,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val dcc: String? = null,
    override val couplingCode: String? = null
) : TestEvent(
    type = Type.PositiveTest,
    eventDate = eventDate,
    testType = testType,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    dcc = dcc,
    couplingCode = couplingCode
)

data class NegativeTest(
    override val eventDate: LocalDate,
    override val country: Country = Country.NL,
    override val testType: TestType,
    override val validFrom: LocalDate? = null,
    override val validUntil: LocalDate? = null,
    override val dcc: String? = null,
    override val couplingCode: String? = null
) : TestEvent(
    type = Type.NegativeTest,
    eventDate = eventDate,
    testType = testType,
    country = country,
    validFrom = validFrom,
    validUntil = validUntil,
    dcc = dcc,
    couplingCode = couplingCode
)
