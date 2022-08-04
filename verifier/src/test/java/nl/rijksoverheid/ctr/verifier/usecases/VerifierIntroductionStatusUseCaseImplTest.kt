package nl.rijksoverheid.ctr.verifier.usecases

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierIntroductionStatusUseCaseImplTest {

    private val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
    private val introductionData: IntroductionData = mockk()

    private val introductionStatusUseCase = VerifierIntroductionStatusUseCaseImpl(
        introductionPersistenceManager, introductionData
    )

    @Test
    fun `when introduction isn't finished, introduction is required`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false

        assertTrue(introductionStatusUseCase.getIntroductionRequired())
    }

    @Test
    fun `when intro is finished, introduction is not required`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns true

        assertFalse(introductionStatusUseCase.getIntroductionRequired())
    }
}
