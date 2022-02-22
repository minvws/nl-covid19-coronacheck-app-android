package nl.rijksoverheid.ctr.verifier.usecase

import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.usecases.IntroductionStatusUseCase


class VerifierIntroductionStatusUseCaseImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val introductionData: IntroductionData,
    private val featureFlagUseCase: FeatureFlagUseCase
) : IntroductionStatusUseCase {

    override fun get(): IntroductionStatus {
        return when {
            introductionIsNotFinished() -> IntroductionStatus.IntroductionNotFinished(
                introductionData
            )
            newFeaturesAvailable() -> IntroductionStatus.IntroductionFinished.NewFeatures(introductionData)
            newTermsAvailable() -> IntroductionStatus.IntroductionFinished.ConsentNeeded(introductionData)
            else -> IntroductionStatus.IntroductionFinished.NoActionRequired
        }
    }

    private fun newTermsAvailable() =
                !introductionPersistenceManager.getNewTermsSeen(introductionData.newTerms.version)

    private fun newFeaturesAvailable() = introductionData.newFeatures.isNotEmpty() &&
            !introductionPersistenceManager.getNewFeaturesSeen(introductionData.newFeatureVersion) && featureFlagUseCase.isVerificationPolicySelectionEnabled()

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
