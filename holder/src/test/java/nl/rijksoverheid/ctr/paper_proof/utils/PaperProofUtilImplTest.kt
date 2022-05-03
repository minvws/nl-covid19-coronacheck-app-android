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
import nl.rijksoverheid.ctr.holder.paper_proof.utils.PaperProofUtilImpl
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaperProofUtilImplTest: AutoCloseKoinTest() {

    @Test
    fun `getInfoScreenFooterText returns correct copy if foreign dcc`() {
        val mobileCoreWrapper: MobileCoreWrapper = mockk()
        every { mobileCoreWrapper.isForeignDcc(any()) } answers { true }

        val util = PaperProofUtilImpl(
            context = ApplicationProvider.getApplicationContext(),
            mobileCoreWrapper = mobileCoreWrapper,
            getDccFromEuropeanCredentialUseCase = mockk()
        )

        Assert.assertEquals(
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

        Assert.assertEquals(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.paper_proof_event_explanation_footer),
            util.getInfoScreenFooterText("".toByteArray())
        )
    }
}