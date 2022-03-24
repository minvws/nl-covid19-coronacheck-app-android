package nl.rijksoverheid.ctr.verifier.usecases

import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase


class VerifierIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData
) : IntroductionStatusUseCase {

    override fun get(): IntroductionStatus {
        return when {
            setupIsNotFinished() -> IntroductionStatus.SetupNotFinished
            introductionIsNotFinished() -> IntroductionStatus.OnboardingNotFinished(introductionData)
            else -> IntroductionStatus.IntroductionFinished
        }
    }

    private fun setupIsNotFinished() = !introductionPersistenceManager.getSetupFinished()

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
