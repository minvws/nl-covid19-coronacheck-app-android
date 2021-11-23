/*
 *
 *  *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.util

import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import org.junit.Test

class DomesticVaccinationRecoveryCombinationUtilImplTest {

    private val appConfigUseCase = mockk<CachedAppConfigUseCase>()
    private val util = DomesticVaccinationRecoveryCombinationUtilImpl(appConfigUseCase)


    @Test
    fun `combination is none without recovery`() {

    }

    @Test
    fun `combination is only vaccination`() {

    }

    @Test
    fun `combination is only recovery`() {

    }

    @Test
    fun `combination is none with recovery`() {

    }
    @Test
    fun `combination is vaccination and recovery`() {

    }

    @Test
    fun `combination is not applicable`() {

    }
}