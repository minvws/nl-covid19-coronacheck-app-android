/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.paper_proof.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetDccFromEuropeanCredentialUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtilImpl
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaperProofUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `getInfoScreenFooterText returns correct copy if foreign dcc`() {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        every { mobileCoreWrapper.isForeignDcc(any()) } answers { true }

        val util = PaperProofUtilImpl(
            context = ApplicationProvider.getApplicationContext(),
            mobileCoreWrapper = mobileCoreWrapper,
            getDccFromEuropeanCredentialUseCase = mockk()
        )

        assertEquals(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.holder_listRemoteEvents_somethingWrong_foreignDCC_body),
            util.getInfoScreenFooterText("".toByteArray()))
    }

    @Test
    fun `getInfoScreenFooterText returns correct copy if dutch dcc`() {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        every { mobileCoreWrapper.isForeignDcc(any()) } answers { false }

        val util = PaperProofUtilImpl(
            context = ApplicationProvider.getApplicationContext(),
            mobileCoreWrapper = mobileCoreWrapper,
            getDccFromEuropeanCredentialUseCase = mockk()
        )

        assertEquals(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.paper_proof_event_explanation_footer),
            util.getInfoScreenFooterText("".toByteArray())
        )
    }

    @Test
    fun `getIssuer returns issuer for vaccinations`() {
        val json = JSONObject()
        val vaccinations = JSONArray()
        val vaccination = JSONObject()
        vaccination.put("is", "Ministry of Health Welfare and Sport")
        vaccinations.put(0, vaccination)
        json.put("v", vaccinations)
        val getDccFromEuropeanCredentialUseCase: GetDccFromEuropeanCredentialUseCase = mockk<GetDccFromEuropeanCredentialUseCase>()
        every { getDccFromEuropeanCredentialUseCase.get(any()) } returns json
        val paperProofUtil = PaperProofUtilImpl(
            context = ApplicationProvider.getApplicationContext(),
            mobileCoreWrapper = mockk(),
            getDccFromEuropeanCredentialUseCase = getDccFromEuropeanCredentialUseCase
        )

        val issuer = paperProofUtil.getIssuer("".toByteArray())

        assertEquals("Ministry of Health Welfare and Sport", issuer)
    }

    @Test
    fun `getIssuer returns issuer for tests`() {
        val json = JSONObject()
        val tests = JSONArray()
        val test = JSONObject()
        test.put("is", "Ministry of Health Welfare and Sport")
        tests.put(0, test)
        json.put("t", tests)
        val getDccFromEuropeanCredentialUseCase: GetDccFromEuropeanCredentialUseCase = mockk<GetDccFromEuropeanCredentialUseCase>()
        every { getDccFromEuropeanCredentialUseCase.get(any()) } returns json
        val paperProofUtil = PaperProofUtilImpl(
            context = ApplicationProvider.getApplicationContext(),
            mobileCoreWrapper = mockk(),
            getDccFromEuropeanCredentialUseCase = getDccFromEuropeanCredentialUseCase
        )

        val issuer = paperProofUtil.getIssuer("".toByteArray())

        assertEquals("Ministry of Health Welfare and Sport", issuer)
    }
}
