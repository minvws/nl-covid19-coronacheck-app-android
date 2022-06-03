package nl.rijksoverheid.ctr.verifier.usecases

import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.status.usecases.IntroductionStatusUseCase


class VerifierIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData
) : IntroductionStatusUseCase {

    override fun getIntroductionRequired() =
        !introductionPersistenceManager.getIntroductionFinished()

    override fun getData(): IntroductionData {
        return introductionData
    }
}
