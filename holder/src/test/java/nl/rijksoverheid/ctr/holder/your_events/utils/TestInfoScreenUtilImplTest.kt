package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class TestInfoScreenUtilImplTest : AutoCloseKoinTest() {

    private val countryUtil: CountryUtil by inject()
    private val paperProofUtil: PaperProofUtil by inject()

    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase = object :
        HolderCachedAppConfigUseCase {
        override fun getCachedAppConfigOrNull(): HolderConfig? {
            return null
        }

        override fun getCachedAppConfig() = HolderConfig.default().copy(
            hpkCodes = listOf(
                AppConfig.HpkCode(
                    code = "2924528",
                    name = "Pfizer (Comirnaty)",
                    displayName = "PFIZER INJVLST 0,3ML",
                    vp = "1119349007",
                    mp = "EU/1/20/1528",
                    ma = "ORG-100030215"
                )
            ),
            euBrands = listOf(
                AppConfig.Code(
                    code = "EU/1/20/1528",
                    name = "Pfizer (Comirnaty)"
                )
            ),
            euManufacturers = listOf(
                AppConfig.Code(
                    code = "ORG-100030215",
                    name = "Biontech Manufacturing GmbH"
                )
            )
        )
    }

    @Test
    fun getForPositiveTest() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources

        val testClock =
            Clock.fixed(Instant.parse("2022-12-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val positiveTest = RemoteEventPositiveTest(
            type = "positivetest",
            unique = "unique",
            isSpecimen = true,
            positiveTest = RemoteEventPositiveTest.PositiveTest(
                sampleDate = OffsetDateTime.now(testClock),
                positiveResult = true,
                facility = "facility",
                type = "PCR",
                name = "testname",
                country = "NL",
                manufacturer = "manufacturer"
            )
        )

        val util =
            TestInfoScreenUtilImpl(resources, paperProofUtil, countryUtil, cachedAppConfigUseCase)

        assertEquals(
            expected = "De volgende gegevens zijn opgehaald bij de testlocatie:<br/><br/>Naam: <b>Onoma Epitheto</b><br/>Geboortedatum: <b>01-08-1982</b><br/><br/>Type test: <b>PCR</b><br/>Testnaam: <b>testname</b><br/>Testdatum: <b>2022-12-01T00:00Z</b><br/>Testuitslag: <b>positief (coronavirus vastgesteld)</b><br/>Testproducent: <b>manufacturer</b><br/>Testlocatie: <b>facility</b><br/>Getest in: <b>Nederland</b><br/><br/>Uniek testnummer: <b>unique</b><br/>",
            actual = util.getForPositiveTest(
                event = positiveTest,
                testDate = OffsetDateTime.now(testClock).toString(),
                fullName = "Onoma Epitheto",
                birthDate = "01-08-1982"
            ).description
        )
    }

    @Test
    fun getForNegativeTest() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources

        val testClock =
            Clock.fixed(Instant.parse("2022-12-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val negativeTest = RemoteEventNegativeTest(
            type = "positivetest",
            unique = "unique",
            isSpecimen = true,
            negativeTest = RemoteEventNegativeTest.NegativeTest(
                sampleDate = OffsetDateTime.now(testClock),
                negativeResult = true,
                facility = "facility",
                type = "PCR",
                name = "testname",
                country = "NL",
                manufacturer = "manufacturer"
            )
        )

        val util =
            TestInfoScreenUtilImpl(resources, paperProofUtil, countryUtil, cachedAppConfigUseCase)

        assertEquals(
            expected = "De volgende gegevens zijn opgehaald bij de testlocatie:<br/><br/>Naam: <b>Onoma Epitheto</b><br/>Geboortedatum: <b>01-08-1982</b><br/><br/>Type test: <b>PCR</b><br/>Testnaam: <b>testname</b><br/>Testdatum: <b>2022-12-01T00:00Z</b><br/>Testuitslag: <b>negatief (geen coronavirus vastgesteld)</b><br/>Testproducent: <b>manufacturer</b><br/>Testlocatie: <b>facility</b><br/>Getest in: <b>Nederland</b><br/><br/>Uniek testnummer: <b>unique</b><br/>",
            actual = util.getForNegativeTest(
                event = negativeTest,
                testDate = OffsetDateTime.now(testClock).toString(),
                fullName = "Onoma Epitheto",
                birthDate = "01-08-1982",
                europeanCredential = null,
                addExplanation = false
            ).description
        )
    }
}
