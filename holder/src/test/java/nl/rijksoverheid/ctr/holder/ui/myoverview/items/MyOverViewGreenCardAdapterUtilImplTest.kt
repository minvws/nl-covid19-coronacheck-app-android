package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtilImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class MyOverViewGreenCardAdapterUtilImplTest : AutoCloseKoinTest() {
    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val credentialUtil = mockk<CredentialUtil>(relaxed = true)
    private val testResultAdapterItemUtil: TestResultAdapterItemUtil = mockk(relaxed = true)
    private val greenCardUtil: GreenCardUtil = mockk(relaxed = true)

    private val myOverViewGreenCardAdapterUtil: MyOverViewGreenCardAdapterUtil by lazy {
        MyOverViewGreenCardAdapterUtilImpl(context, credentialUtil, testResultAdapterItemUtil, greenCardUtil)
    }

    private val viewBinding = object : ViewBindingWrapper {

        private val titleText: TextView = TextView(context)
        private val layout = LinearLayout(context)
        private val lastText = TextView(context)

        override val title: TextView
            get() = titleText
        override val description: LinearLayout
            get() = layout
        override val expiresIn: TextView
            get() = lastText
    }

    @Before
    fun setup() {
        viewBinding.description.removeAllViews()
        viewBinding.expiresIn.visibility = View.GONE
    }

    @Test
    fun europeanTest() {
        every { credentialUtil.getTestTypeForEuropeanCredentials(any()) } returns "NAAT"
        val greenCard = greenCard(GreenCardType.Eu)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Testbewijs: PCR (NAAT)", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("Testdatum: dinsdag 27 juli 11:10", (viewBinding.description.getChildAt(1) as TextView).text)
    }

    @Test
    fun domesticTest() {
        val greenCard = greenCard(GreenCardType.Domestic)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Testbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig tot woensdag 28 juli 21:06", (viewBinding.description.getChildAt(1) as TextView).text)
    }

    @Test
    fun europeanVaccination() {
        every { credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any()) } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("dosis 2 van 2", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("Vaccinatiedatum: 27 juli 2021", (viewBinding.description.getChildAt(1) as TextView).text)
    }

    @Test
    fun europeanVaccinationFuture() {
        every { credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any()) } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("dosis 2 van 2", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("Vaccinatiedatum: 27 juli 2021", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticVaccination() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Vaccinatiebewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig vanaf 27 juli 2021   ", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticVaccinationFuture() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first())))))

        assertEquals("Vaccinatiebewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig vanaf 27 juli 11:11   ", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals("Wordt automatisch geldig", viewBinding.expiresIn.text)
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun europeanRecovery() {
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Herstelbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig tot 28 jul 2021", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun europeanRecoveryFuture() {
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first())))))

        assertEquals("Herstelbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig vanaf 27 juli 11:11 t/m 28 juli 2021", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecovery() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Herstelbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig tot 28 jul 2021", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecoveryFuture() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first())))))

        assertEquals("Herstelbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig vanaf 27 juli 11:11 t/m 28 juli 2021", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticTestExpiringIn6Minutes() {
        val testResultAdapterItemUtil = TestResultAdapterItemUtilImpl(Clock.fixed(Instant.ofEpochSecond(1627495600), ZoneId.of("UTC")))
        val greenCard = greenCard(GreenCardType.Domestic)
        every { greenCardUtil.getExpireDate(greenCard) } returns greenCard.credentialEntities.first().expirationTime
        val myOverViewGreenCardAdapterUtil = MyOverViewGreenCardAdapterUtilImpl(context, credentialUtil, testResultAdapterItemUtil, greenCardUtil)

        myOverViewGreenCardAdapterUtil.setContent(viewBinding, listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))))

        assertEquals("Testbewijs:", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals("geldig tot woensdag 28 juli 21:06", (viewBinding.description.getChildAt(1) as TextView).text)
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
        assertEquals("Verloopt in 1 uur 1 min", viewBinding.expiresIn.text)
    }

    @Test
    fun `Title should be specific for EU vaccination card`() {
        every {
            credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any())
        } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding = viewBinding,
            cards = listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))),
        )

        assertEquals("Vaccinatiebewijs", (viewBinding.title).text)
    }

    @Test
    fun `Title should be generic for EU test and recovery`() {
        val testCard = greenCard(GreenCardType.Eu, OriginType.Test)
        val recoveryCard = greenCard(GreenCardType.Eu, OriginType.Recovery)

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(AdapterCard(testCard, listOf(OriginState.Valid(testCard.origins.first()))))
        )
        assertEquals("Mijn bewijs", (viewBinding.title).text)

        // reset
        viewBinding.title.text = ""

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(AdapterCard(recoveryCard, listOf(OriginState.Valid(testCard.origins.first()))))
        )
        assertEquals("Mijn bewijs", (viewBinding.title).text)
    }

    @Test
    fun `Additional cards of the same type should be shown in the description`() {
        every {
            credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any())
        } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding = viewBinding,
            cards = listOf(
                AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))),
                AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))),
                AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first())))
            ),
        )

        assertEquals(
            "dosis 2 van 2",
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "Vaccinatiedatum: 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            "dosis 2 van 2",
            (viewBinding.description.getChildAt(2) as TextView).text
        )
        assertEquals(
            "Vaccinatiedatum: 27 juli 2021",
            (viewBinding.description.getChildAt(3) as TextView).text
        )
        assertEquals(
            "dosis 2 van 2",
            (viewBinding.description.getChildAt(4) as TextView).text
        )
        assertEquals(
            "Vaccinatiedatum: 27 juli 2021",
            (viewBinding.description.getChildAt(5) as TextView).text
        )
    }

    private fun greenCard(
        greenCardType: GreenCardType,
        originType: OriginType = OriginType.Test
    ): GreenCard {
        // 2021-07-27T09:10Z
        val eventTime =
            OffsetDateTime.now(Clock.fixed(Instant.ofEpochSecond(1627377000), ZoneId.of("UTC")))
        // 2021-07-27T09:11:40Z
        val validFrom =
            OffsetDateTime.now(Clock.fixed(Instant.ofEpochSecond(1627377100), ZoneId.of("UTC")))
        // 2021-07-28T21:06:20Z
        val expirationTime =
            OffsetDateTime.now(Clock.fixed(Instant.ofEpochSecond(1627499200), ZoneId.of("UTC")))
        val credentialEntity = CredentialEntity(
            id = 1,
            greenCardId = 1,
            data = "".toByteArray(),
            credentialVersion = 2,
            validFrom = validFrom,
            expirationTime = expirationTime,
        )

        val originEntity = OriginEntity(
            id = 1,
            greenCardId = 1,
            type = originType,
            eventTime = eventTime,
            expirationTime = expirationTime,
            validFrom = validFrom,
        )

        return GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = greenCardType
            ),
            origins = listOf(originEntity),
            credentialEntities = listOf(credentialEntity),
        )
    }
}
