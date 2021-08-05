/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.mockk
import junit.framework.Assert.assertEquals
import nl.rijksoverheid.ctr.holder.fakeCachedAppConfigUseCase
import org.junit.Test
import java.util.*

class InfoScreenUtilImplTest {

    private val infoScreenUtil =
        InfoScreenUtilImpl(mockk(), mockk(), fakeCachedAppConfigUseCase())

    @Test
    fun `getCountry returns correct strings for the Netherlands in Dutch locale`() {
        val dutchString = infoScreenUtil.getCountry("NL", Locale("nl", "nl"))
        assertEquals("Nederland / The Netherlands", dutchString)
    }

    @Test
    fun `getCountry returns correct strings for Belgium in Dutch locale`() {
        val belgianString = infoScreenUtil.getCountry("be", Locale("nl", "nl"))
        assertEquals("België / Belgium", belgianString)
    }

    @Test
    fun `getCountry returns correct strings for the Netherlands in English locale`() {
        val dutchString = infoScreenUtil.getCountry("nl", Locale("en", "en"))
        assertEquals("Netherlands", dutchString)
    }

}