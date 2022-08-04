/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.dashboard.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import nl.rijksoverheid.ctr.holder.HolderApplication
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.items.AdapterCard
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemBindingWrapper
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtil
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemExpiryUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.items.DashboardGreenCardAdapterItemUtilImpl
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.ext.capitalize
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
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
class DashboardGreenCardAdapterItemUtilImplTest : AutoCloseKoinTest() {

    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val credentialUtil = mockk<CredentialUtil>(relaxed = true)
    private val dashboardGreenCardAdapterItemExpiryUtil: DashboardGreenCardAdapterItemExpiryUtil =
        mockk(relaxed = true)
    private val greenCardUtil: GreenCardUtil = mockk(relaxed = true)

    private val viewBinding = object : DashboardGreenCardAdapterItemBindingWrapper {

        private val titleText: TextView = TextView(context)
        private val layout = LinearLayout(context)
        private val lastText = TextView(context)
        private val policyLabelText = TextView(context)

        override val title: TextView
            get() = titleText
        override val description: LinearLayout
            get() = layout
        override val expiresIn: TextView
            get() = lastText
        override val policyLabel: TextView
            get() = policyLabelText
    }

    private val dosisString = "dosis 2 van 2"

    @Before
    fun setup() {
        viewBinding.description.removeAllViews()
        viewBinding.expiresIn.visibility = View.GONE
    }

    @Test
    fun europeanTest() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )
        val testType = "PCR (NAAT)"
        every { credentialUtil.getTestTypeForEuropeanCredentials(any()) } returns testType
        val greenCard = greenCard(GreenCardType.Eu)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            "${context.getString(R.string.qr_card_test_eu)} $testType",
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.qr_card_test_title_eu)} dinsdag 27 juli 11:10",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    @Test
    fun domesticTest() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Domestic)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
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
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        every {
            credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(
                any(),
                any(),
                any()
            )
        } returns dosisString
        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Vaccination))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(dosisString, (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
    }

    @Test
    fun europeanVaccinationFuture() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        every {
            credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(
                any(),
                any(),
                any()
            )
        } returns dosisString
        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Vaccination))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(dosisString, (viewBinding.description.getChildAt(0) as TextView).text)
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun `domesticVaccination that expires in 3 years`() {
        // Today is 2021-01-01
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        // Expiration time is three years from today
        val expirationTime =
            OffsetDateTime.ofInstant(Instant.parse("2024-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            clock,
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard =
            greenCard(GreenCardType.Domestic, listOf(OriginType.Vaccination), expirationTime)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
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
    fun `domesticVaccination that has a validity of more than 3 years`() {
        // Today is 2021-01-01
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        // Expiration time is four years from today
        val expirationTime =
            OffsetDateTime.ofInstant(Instant.parse("2025-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            clock,
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard =
            greenCard(GreenCardType.Domestic, listOf(OriginType.Vaccination), expirationTime)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
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
    fun `domesticVaccination that has a validity of less than 3 years`() {
        // Today is 2021-01-01
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        // Expiration time is two years from today
        val expirationTime =
            OffsetDateTime.ofInstant(Instant.parse("2022-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            clock,
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard =
            greenCard(GreenCardType.Domestic, listOf(OriginType.Vaccination), expirationTime)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_vaccination_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "1 januari 2022"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun `domesticVaccinationFuture that has a validity of more than 3 years`() {
        // Today is 2021-01-01
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        // Expiration time is three years from today
        val expirationTime =
            OffsetDateTime.ofInstant(Instant.parse("2024-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            clock,
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard =
            greenCard(GreenCardType.Domestic, listOf(OriginType.Vaccination), expirationTime)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Future(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
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
    fun `domesticVaccinationFuture that has a validity of 2 years`() {
        // Today is 2021-01-01
        val clock = Clock.fixed(Instant.parse("2021-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        // Expiration time is three years from today
        val expirationTime =
            OffsetDateTime.ofInstant(Instant.parse("2023-01-01T00:00:00.00Z"), ZoneId.of("UTC"))

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            clock,
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard =
            greenCard(GreenCardType.Domestic, listOf(OriginType.Vaccination), expirationTime)
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Future(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_vaccination_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "${
                context.getString(
                    R.string.qr_card_validity_future_from,
                    "27 juli 11:11",
                    ""
                )
            }${context.getString(R.string.qr_card_validity_future_until, "1 januari 2023")}",
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
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Recovery))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "28 juli 2021"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun europeanRecoveryFuture() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Recovery))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Future(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            "${
                context.getString(
                    R.string.qr_card_validity_future_from,
                    "27 juli 11:11",
                    ""
                )
            }${context.getString(R.string.qr_card_validity_future_until, "28 juli 2021")}",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecovery() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_recovery_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "28 juli 2021"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticRecoveryFuture() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Future(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_recovery_title_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "${
                context.getString(
                    R.string.qr_card_validity_future_from,
                    "27 juli 11:11",
                    ""
                )
            }${context.getString(R.string.qr_card_validity_future_until, "28 juli 2021")}",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.VISIBLE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticTestExpiringIn6Minutes() {
        val testResultAdapterItemUtil = DashboardGreenCardAdapterItemExpiryUtilImpl(
            Clock.fixed(
                Instant.ofEpochSecond(1627495600),
                ZoneId.of("UTC")
            ), context
        )
        val greenCard = greenCard(GreenCardType.Domestic)
        every {
            greenCardUtil.getExpireDate(
                greenCard,
                OriginType.Test
            )
        } returns greenCard.credentialEntities.first().expirationTime
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            testResultAdapterItemUtil
        )

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.qr_card_test_domestic),
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            context.getString(
                R.string.my_overview_test_result_expires_in_hours_minutes,
                "1 uur",
                "0 minuten"
            ), viewBinding.expiresIn.text
        )
    }

    @Test
    fun `Title should be specific for EU vaccination card`() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        every {
            credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(any(), any(), any())
        } returns dosisString
        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Vaccination))
        myOverViewGreenCardAdapterUtil.setContent(
            dashboardGreenCardAdapterItemBinding = viewBinding,
            cards = listOf(
                AdapterCard(
                    greenCard,
                    listOf(OriginState.Valid(greenCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            context.getString(R.string.type_vaccination).capitalize(),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Title should be specific for EU test`() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val testCard = greenCard(GreenCardType.Eu, listOf(OriginType.Test))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    testCard,
                    listOf(OriginState.Valid(testCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )
        assertEquals(context.getString(R.string.type_test).capitalize(), (viewBinding.title).text)
    }

    @Test
    fun `Title should be specific for EU recovery`() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val recoveryCard = greenCard(GreenCardType.Eu, listOf(OriginType.Recovery))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )
        assertEquals(
            context.getString(R.string.type_recovery).capitalize(),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Title should be specific for domestic 3g card`() {
        val context = ApplicationProvider.getApplicationContext<HolderApplication>()

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val recoveryCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )
        assertEquals(
            context.getString(R.string.holder_dashboard_domesticQRCard_3G_title),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Title should be specific for domestic 1g card`() {
        val context = ApplicationProvider.getApplicationContext<HolderApplication>()

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val recoveryCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first())),
                    GreenCardDisclosurePolicy.OneG
                )
            )
        )
        assertEquals(
            context.getString(R.string.holder_dashboard_domesticQRCard_1G_title),
            (viewBinding.title).text
        )
    }

    @Test
    fun `Policy label should be specific for domestic 1g card`() {
        val context = ApplicationProvider.getApplicationContext<HolderApplication>()

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val recoveryCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first())),
                    GreenCardDisclosurePolicy.OneG
                )
            )
        )
        assertEquals(
            context.getString(R.string.holder_dashboard_domesticQRCard_1G_label),
            (viewBinding.policyLabel).text
        )
    }

    @Test
    fun `Policy label should be specific for domestic 3g card`() {
        val context = ApplicationProvider.getApplicationContext<HolderApplication>()

        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val recoveryCard = greenCard(GreenCardType.Domestic, listOf(OriginType.Recovery))

        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    recoveryCard,
                    listOf(OriginState.Valid(recoveryCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )
        assertEquals(
            context.getString(R.string.holder_dashboard_domesticQRCard_3G_label),
            (viewBinding.policyLabel).text
        )
    }

    @Test
    fun `Additional cards of the same type should be shown in the description`() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        every {
            credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(any(), any(), any())
        } returns dosisString
        val greenCard = greenCard(GreenCardType.Eu, listOf(OriginType.Vaccination))
        myOverViewGreenCardAdapterUtil.setContent(
            dashboardGreenCardAdapterItemBinding = viewBinding,
            cards = listOf(
                AdapterCard(
                    greenCard,
                    listOf(OriginState.Valid(greenCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                ),
                AdapterCard(
                    greenCard,
                    listOf(OriginState.Valid(greenCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                ),
                AdapterCard(
                    greenCard,
                    listOf(OriginState.Valid(greenCard.origins.first())),
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            dosisString,
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            dosisString,
            (viewBinding.description.getChildAt(2) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(3) as TextView).text
        )
        assertEquals(
            dosisString,
            (viewBinding.description.getChildAt(4) as TextView).text
        )
        assertEquals(
            "${context.getString(R.string.qr_card_vaccination_title_eu)} 27 juli 2021",
            (viewBinding.description.getChildAt(5) as TextView).text
        )
    }

    @Test
    fun domesticVaccinationAssessment() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(GreenCardType.Domestic, listOf(OriginType.VaccinationAssessment))
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding, listOf(
                AdapterCard(
                    greenCard, listOf(
                        OriginState.Valid(greenCard.origins.first())
                    ), GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            "${context.getString(R.string.general_visitorPass).capitalize()}:",
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(View.GONE, viewBinding.expiresIn.visibility)
    }

    @Test
    fun domesticVaccinationAssessmentAndTest() {
        val myOverViewGreenCardAdapterUtil = DashboardGreenCardAdapterItemUtilImpl(
            Clock.systemUTC(),
            context,
            credentialUtil,
            dashboardGreenCardAdapterItemExpiryUtil
        )

        val greenCard = greenCard(
            GreenCardType.Domestic,
            listOf(OriginType.VaccinationAssessment, OriginType.Test)
        )
        myOverViewGreenCardAdapterUtil.setContent(
            viewBinding,
            listOf(
                AdapterCard(
                    greenCard,
                    greenCard.origins.map { OriginState.Valid(it) },
                    GreenCardDisclosurePolicy.ThreeG
                )
            )
        )

        assertEquals(
            "${context.getString(R.string.general_visitorPass).capitalize()}:",
            (viewBinding.description.getChildAt(0) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(1) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_test_domestic).capitalize(),
            (viewBinding.description.getChildAt(2) as TextView).text
        )
        assertEquals(
            context.getString(R.string.qr_card_validity_valid, "woensdag 28 juli 21:06"),
            (viewBinding.description.getChildAt(3) as TextView).text
        )
    }

    private fun greenCard(
        greenCardType: GreenCardType,
        originTypes: List<OriginType> = listOf(OriginType.Test),
        expirationTime: OffsetDateTime = OffsetDateTime.now(
            Clock.fixed(
                Instant.ofEpochSecond(
                    1627499200
                ), ZoneId.of("UTC")
            )
        ) // 2021-07-28T21:06:20Z
    ): GreenCard {
        // 2021-07-27T09:10Z
        val eventTime =
            OffsetDateTime.now(Clock.fixed(Instant.ofEpochSecond(1627377000), ZoneId.of("UTC")))
        // 2021-07-27T09:11:40Z
        val validFrom =
            OffsetDateTime.now(Clock.fixed(Instant.ofEpochSecond(1627377100), ZoneId.of("UTC")))
        val credentialEntity = CredentialEntity(
            id = 1,
            greenCardId = 1,
            data = "".toByteArray(),
            credentialVersion = 2,
            validFrom = validFrom,
            expirationTime = expirationTime
        )

        val originEntities = originTypes.map {
            OriginEntity(
                id = 1,
                greenCardId = 1,
                type = it,
                eventTime = eventTime,
                expirationTime = expirationTime,
                validFrom = validFrom
            )
        }

        return GreenCard(
            greenCardEntity = GreenCardEntity(
                id = 1,
                walletId = 1,
                type = greenCardType
            ),
            origins = originEntities,
            credentialEntities = listOf(credentialEntity)
        )
    }
}
