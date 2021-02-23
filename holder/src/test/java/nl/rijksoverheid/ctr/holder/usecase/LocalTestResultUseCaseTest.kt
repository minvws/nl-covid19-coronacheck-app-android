package nl.rijksoverheid.ctr.holder.usecase

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.robolectric.RobolectricTestRunner

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

@RunWith(RobolectricTestRunner::class)
class LocalTestResultUseCaseTest : AutoCloseKoinTest() {

    @Test
    fun `No local test result saved returns None`() =
        runBlocking {
            val usecase: LocalTestResultUseCase = get()
            val localTestResult = usecase.get()
            Assert.assertTrue(localTestResult is LocalTestResultState.None)
        }
}
