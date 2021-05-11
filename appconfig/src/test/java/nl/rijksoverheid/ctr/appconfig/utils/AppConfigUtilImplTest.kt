package nl.rijksoverheid.ctr.appconfig.utils

import android.content.Context
import io.mockk.InternalPlatformDsl.toStr
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.AppConfigUtilImpl
import nl.rijksoverheid.ctr.appconfig.fakeCachedAppConfigUseCase
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppConfigUtilImplTest {

    @Test
    fun `getStringWithTestValidity returns string with configurable integer`() {
        val textResource = 0
        val maxValidityHours = 48
        val mockedContext: Context = mockk(relaxed = true)

        val appConfigUtil = AppConfigUtilImpl(
            context = mockedContext,
            cachedAppConfigUseCase = fakeCachedAppConfigUseCase(
                cachedAppConfigMaxValidityHours = maxValidityHours
            )
        )

        appConfigUtil.getStringWithTestValidity(textResource)
        verify { mockedContext.getString(textResource, maxValidityHours.toStr()) }
    }

}
