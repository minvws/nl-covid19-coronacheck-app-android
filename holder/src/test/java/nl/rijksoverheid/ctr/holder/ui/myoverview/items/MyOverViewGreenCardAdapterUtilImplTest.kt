package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeGreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
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
    private val featureFlagUseCase: FeatureFlagUseCase = mockk(relaxed = true)

    private val myOverViewGreenCardAdapterUtil: MyOverViewGreenCardAdapterUtil by lazy {
        MyOverViewGreenCardAdapterUtilImpl(
            context, credentialUtil, testResultAdapterItemUtil, greenCardUtil, featureFlagUseCase
        )
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

        every { featureFlagUseCase.isVerificationPolicyEnabled() } answers { true }
    }

    @Test
    fun europeanTest() {
        every { credentialUtil.getTestTypeForEuropeanCredentials(any()) } returns "PCR (NAAT)"
        val greenCard = greenCard(GreenCardType.Eu)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            "${context.getString(R.string.qr_card_test_eu)} PCR (NAAT)",
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.your_test_result_explanation_description_test_date)} dinsdag 27 juli 11:10",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    @Test
    fun domesticTest() {
        val greenCard = greenCard(GreenCardType.Domestic)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_test_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    @Test
    fun europeanVaccination() {
        every {
            credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any())
        } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals("dosis 2 van 2", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    @Test
    fun europeanVaccinationFuture() {
        every {
            credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any())
        } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals("dosis 2 van 2", (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticVaccination() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_vaccination_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_future_from, "27 juli 2021", ""),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticVaccinationFuture() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_vaccination_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_future_from, "27 juli 11:11", ""),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_future),
            viewBinding.expiresIn.text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun europeanRecovery() {
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "28 jul 2021"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun europeanRecoveryFuture() {
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(
                R.string.qr_card_validity_future_from, "27 juli 11:11",
                context.getString(R.string.qr_card_validity_future_until, "28 juli 2021")
            ), (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecovery() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_recovery_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "28 jul 2021"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecoveryFuture() {
        val greenCard = greenCard(GreenCardType.Domestic, OriginType.Recovery)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Future(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_recovery_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(
                R.string.qr_card_validity_future_from, "27 juli 11:11",
                context.getString(R.string.qr_card_validity_future_until, "28 juli 2021")
            ), (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticTestExpiringIn6Minutes() {
        val testResultAdapterItemUtil = TestResultAdapterItemUtilImpl(
            Clock.fixed(
                Instant.ofEpochSecond(1627495600),
                ZoneId.of("UTC")
            )
        )
        val greenCard = greenCard(GreenCardType.Domestic)
        every { greenCardUtil.getExpireDate(greenCard) } returns greenCard.credentialEntities.first().expirationTime
        val myOverViewGreenCardAdapterUtil = MyOverViewGreenCardAdapterUtilImpl(
            context, credentialUtil, testResultAdapterItemUtil, greenCardUtil, featureFlagUseCase
        )

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_test_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
        assertEquals(
            context.getString(
                R.string.my_overview_test_result_expires_in_hours_minutes,
                "1",
                "1"
            ), viewBinding.expiresIn.text
        )
    }

    @Test
    fun `Title should be specific for EU vaccination card`() {
        every {
            credentialUtil.getVaccinationDosesForEuropeanCredentials(any(), any())
        } returns "dosis 2 van 2"
        val greenCard = greenCard(GreenCardType.Eu, OriginType.Vaccination)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding = viewBinding,
            cards = listOf(
                AdapterCard(
                    greenCard,
                    listOf(OriginState.Valid(greenCard.origins.first()))
                )
            ),
        )

        assertEquals(
            context.getString(R.string.qr_code_type_vaccination_title),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Title should be specific for EU test`() {
        val testCard = greenCard(GreenCardType.Eu, OriginType.Test)

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(testCard, listOf(OriginState.Valid(testCard.origins.first()))))
        )
        assertEquals(
            context.getString(R.string.qr_code_type_negative_test_title),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Title should be specific for EU recovery`() {
        val recoveryCard = greenCard(GreenCardType.Eu, OriginType.Recovery)

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first()))
                )
            )
        )
        assertEquals(
            context.getString(R.string.qr_code_type_recovery_title),
            (viewBinding.title).text
        )
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
            "${context.getString(R.string.your_vaccination_explanation_vaccination_date)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            "dosis 2 van 2",
            (viewBinding.description.getChildAt(2) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.your_vaccination_explanation_vaccination_date)} 27 juli 2021",
            (viewBinding.description.getChildAt(3) as TextView).text
        )
        assertEquals(
            "dosis 2 van 2",
            (viewBinding.description.getChildAt(4) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.your_vaccination_explanation_vaccination_date)} 27 juli 2021",
            (viewBinding.description.getChildAt(5) as TextView).text
        )
    }

    @Test
    fun `domestic test with 3G validity`() {
        val greenCard =
            greenCard(GreenCardType.Domestic, category = Mobilecore.VERIFICATION_POLICY_3G)

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(AdapterCard(greenCard, listOf(OriginState.Valid(greenCard.origins.first()))))
        )

        assertEquals(
            context.getString(R.string.qr_card_test_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(
                R.string.holder_my_overview_test_result_validity_3g,
                "woensdag 28 juli 21:06"
            ), (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    private fun greenCard(
        greenCardType: GreenCardType,
        originType: OriginType = OriginType.Test,
        category: String = "2"
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

        return fakeGreenCard(
            greenCardType = greenCardType,
            originType = originType,
            eventTime = eventTime,
            validFrom = validFrom,
            expirationTime = expirationTime,
            category = category
        )
    }
}
