package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class TokenValidatorUtilImplTest {

    private val util = TokenValidatorUtilImpl()

    @Test
    fun `Check valid token`() {
        assertEquals(true, util.validate("2SX4XLGGXUB6V9", "42"))
    }

    @Test
    fun `Check valid token 2`() {
        assertEquals(true, util.validate("YL8BSX9T6J39C7", "Q2"))

    }

    @Test
    fun `Check valid token 3`() {
        assertEquals(true, util.validate("2FR36XSUGJY3UZ", "G2"))

    }

    @Test
    fun `Check valid token 4`() {
        assertEquals(true, util.validate("32X4RUBC2TYBX6", "U2"))

    }

    @Test
    fun `Check invalid token`() {
        assertEquals(false, util.validate("YL8BSX9T6J39C7", "L2"))
    }

    @Test
    fun `Check invalid token 2`() {
        assertEquals(false, util.validate("YL8BSX9T6J39C7", "L"))
    }

    @Test
    fun `Check invalid token 3`() {
        assertEquals(false, util.validate("YL8BSX9T6J39C7", ""))
    }
}
