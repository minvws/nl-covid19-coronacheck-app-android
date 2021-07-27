package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import nl.rijksoverheid.ctr.design.views.ButtonWithProgressWidget
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginUtil
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class MyOverviewGreenCardAdapterItemTest: AutoCloseKoinTest() {
    val readEuropeanCredentialVaccination = "{\"credentialVersion\":1,\"issuer\":\"NL\",\"issuedAt\":1627294308,\"expirationTime\":1629717843,\"dcc\":{\"ver\":\"1.3.0\",\"dob\":\"1960-01-01\",\"nam\":{\"fn\":\"Bouwer\",\"fnt\":\"BOUWER\",\"gn\":\"Bob\",\"gnt\":\"BOB\"},\"v\":[{\"tg\":\"840539006\",\"vp\":\"1119349007\",\"mp\":\"EU\\/1\\/20\\/1528\",\"ma\":\"ORG-100030215\",\"dn\":1,\"sd\":1,\"dt\":\"2021-07-18\",\"co\":\"NL\",\"is\":\"Ministry of Health Welfare and Sport\",\"ci\":\"URN:UCI:01:NL:FE6BOX7GLBBZTH6K5OFO42#1\"}],\"t\":null,\"r\":null}}"

    private val testResultAdapterItemUtil: TestResultAdapterItemUtil = mockk(relaxed = true)
    private val greenCardUtil: GreenCardUtil = mockk(relaxed = true)
    private val credentialUtil: CredentialUtil = mockk(relaxed = true)
    private val originUtil: OriginUtil = mockk(relaxed = true)

    private val context = ApplicationProvider.getApplicationContext<Context>()

//    private val proof1Title = TextView(context)
//    private val proof2Title = TextView(context)
//    private val proof3Title = TextView(context)
//    private val proof1Subtitle = TextView(context)
//    private val proof2Subtitle = TextView(context)
//    private val proof3Subtitle = TextView(context)
//
//    fun openApp() {
//        loadKoinModules(
//            module(override = true) {
//                factory {
//                    testResultAdapterItemUtil
//                }
//                factory {
//                    greenCardUtil
//                }
//                factory {
//                    credentialUtil
//                }
//                factory {
//                    originUtil
//                }
//                factory {
//                    mockk<HolderDatabase>(relaxed = true)
//                }
//            }
//        )
//
//        ActivityScenario.launch<HolderMainActivity>(
//            Intent(
//                ApplicationProvider.getApplicationContext(),
//                HolderMainActivity::class.java
//            )
//        )
//    }

    @Test
    fun `domestic test`() {

    }

    @Test
    fun `domestic vaccination`() {

    }

    @Test
    fun `domestic recovery`() {

    }

//    @Test
//    fun `european test`() {
//        val credentialEntity = CredentialEntity(
//            id = 1,
//            greenCardId = 1,
//            data = "".toByteArray(),
//            credentialVersion = 2,
//            validFrom = OffsetDateTime.now(),
//            expirationTime = OffsetDateTime.now(),
//        )
//
//        val originEntity = OriginEntity(
//            id = 1,
//            greenCardId = 1,
//            type = OriginType.Test,
//            eventTime = OffsetDateTime.now(),
//            expirationTime = OffsetDateTime.now(),
//            validFrom = OffsetDateTime.now(),
//        )
//
//        val testEuropeanGreenCard = GreenCard(
//            greenCardEntity = GreenCardEntity(
//                id = 1,
//                walletId = 1,
//                type = GreenCardType.Eu
//            ),
//            origins = listOf(),
//            credentialEntities = listOf(credentialEntity),
//        )
//
//        val originStates = listOf(OriginState.Valid(originEntity))
//
//        val credentialState = MyOverviewItem.GreenCardItem.CredentialState.HasCredential(credentialEntity)
//
//        val item = MyOverviewGreenCardAdapterItem(testEuropeanGreenCard, originStates, credentialState, onButtonClick = {_, _ -> })
//
//        openApp()
//
//        val root = mockk<ConstraintLayout>(relaxed = true).apply {
////        every { context } returns context
//
//            every { findViewById<TextView>(R.id.type_title) } returns TextView(context)
//            every { findViewById<ButtonWithProgressWidget>(R.id.buttonWithProgressWidgetContainer) } returns ButtonWithProgressWidget(context)
//            every { findViewById<ImageView>(R.id.imageView) } returns ImageView(context)
//
//            every { findViewById<TextView>(R.id.proof1_title) } returns proof1Title
//            every { findViewById<TextView>(R.id.proof2_title) } returns proof2Title
//            every { findViewById<TextView>(R.id.proof3_title) } returns proof3Title
//            every { findViewById<TextView>(R.id.proof1_subtitle) } returns proof1Subtitle
//            every { findViewById<TextView>(R.id.proof2_subtitle) } returns proof2Subtitle
//            every { findViewById<TextView>(R.id.proof3_subtitle) } returns proof3Subtitle
//        }
//
//        val binding = ItemMyOverviewGreenCardBinding.bind(root)
//
//        item.bind(binding, 0)
//    }

    @Test
    fun `european vaccination`() {
        println(context.getString(R.string.qr_card_validity_future_from))
    }

    @Test
    fun `european recovery`() {

    }
}
