package nl.rijksoverheid.ctr.verifier.usecases

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
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
    fun `when setup isn't finished, the status is setup not finished`() {
        every { introductionPersistenceManager.getSetupFinished() } returns false

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.SetupNotFinished
        )
    }

    @Test
    fun `when introduction isn't finished, the status is introduction not finished`() {
        every { introductionPersistenceManager.getSetupFinished() } returns true
        every { introductionPersistenceManager.getIntroductionFinished() } returns false

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.OnboardingNotFinished(introductionData)
        )
    }
}