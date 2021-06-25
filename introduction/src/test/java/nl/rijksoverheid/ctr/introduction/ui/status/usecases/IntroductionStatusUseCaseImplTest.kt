package nl.rijksoverheid.ctr.introduction.ui.status.usecases

import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import org.junit.Test

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionStatusUseCaseImplTest {

    private val introductionPersistenceManager: IntroductionPersistenceManager = mockk()
    private val introductionData: IntroductionData = mockk()

    private val introductionStatusUseCase = IntroductionStatusUseCaseImpl(
        introductionPersistenceManager, introductionData
    )

    @Test
    fun `when introduction isn't finished, the status is introduction not finished`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns false

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.IntroductionNotFinished(introductionData)
        )
    }

    @Test
    fun `when intro is finished and new features are available, the status is new features`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionData.newFeatures } returns listOf(mockk())
        every { introductionData.newFeatureVersion } returns 2
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns false

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.IntroductionFinished.NewFeatures(introductionData)
        )
    }

    @Test
    fun `when intro is finished and new terms are available, the status is consent needed`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionData.newFeatures } returns emptyList()
        every { introductionData.newFeatureVersion } returns 2
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns true
        every { introductionData.newTerms } returns mockk { every { version } returns 1 }
        every { introductionPersistenceManager.getNewTermsSeen(1) } returns false

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.IntroductionFinished.ConsentNeeded(introductionData)
        )
    }

    @Test
    fun `when intro is finished and there are no new features or terms, the status is no action required`() {
        every { introductionPersistenceManager.getIntroductionFinished() } returns true
        every { introductionData.newFeatures } returns listOf(mockk())
        every { introductionData.newFeatureVersion } returns 2
        every { introductionPersistenceManager.getNewFeaturesSeen(2) } returns true
        every { introductionData.newTerms } returns mockk { every { version } returns 1 }
        every { introductionPersistenceManager.getNewTermsSeen(1) } returns true

        assertEquals(
            introductionStatusUseCase.get(),
            IntroductionStatus.IntroductionFinished.NoActionRequired
        )
    }
}