package nl.rijksoverheid.ctr.shared.utils

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class WebsiteEnvironmentUtilImplTest {

    private val context = mockk<Context>()
    private val url = "https://www.coronacheck.nl/en/faq-in-app.html"
    private val websiteEnvironmentUtil = WebsiteEnvironmentUtilImpl(context)

    @Test
    fun `given a coronacheck url, when acceptance, then return acc web url`() {
        every { context.packageName } returns "nl.rijksoverheid.ctr.holder.acc"

        assert(websiteEnvironmentUtil.adjust(url) == "https://web.acc.coronacheck.nl/en/faq-in-app.html")
    }

    @Test
    fun `given a coronacheck url, when test, then return test web url`() {
        every { context.packageName } returns "nl.rijksoverheid.ctr.holder.tst"

        assert(websiteEnvironmentUtil.adjust(url) == "https://web.test.coronacheck.nl/en/faq-in-app.html")
    }

    @Test
    fun `given a coronacheck url, when prod, then return prod web url`() {
        every { context.packageName } returns "nl.rijksoverheid.ctr.holder"

        assert(websiteEnvironmentUtil.adjust(url) == "https://www.coronacheck.nl/en/faq-in-app.html")
    }

    @Test
    fun `given a link in html text, when acc, then return acc web link`() {
        every { context.packageName } returns "nl.rijksoverheid.ctr.holder.acc"

        val adjustedUrlText = websiteEnvironmentUtil.adjust("In de <a href=\"https://coronacheck.nl/nl/privacy-in-app\">privacyverklaring</a> staat hoe de app omgaat met jouw gegevens. Dit zijn de belangrijkste punten:")
        assert(adjustedUrlText.contains("acc"))
    }
}
