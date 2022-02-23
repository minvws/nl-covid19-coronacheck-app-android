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
            introductionIsNotFinished() -> IntroductionStatus.OnboardingNotFinished(
                introductionData
            )
            newFeaturesAvailable() -> IntroductionStatus.OnboardingFinished.NewFeatures(introductionData)
            newTermsAvailable() -> IntroductionStatus.OnboardingFinished.ConsentNeeded(introductionData)
            else -> IntroductionStatus.IntroductionFinished
        }
    }

    private fun newTermsAvailable() =
                !introductionPersistenceManager.getNewTermsSeen(introductionData.newTerms.version)

    private fun newFeaturesAvailable(): Boolean {
        val newFeatureVersion = introductionData.newFeatureVersion
        return introductionData.newFeatures.isNotEmpty() &&
                newFeatureVersion != null &&
                !introductionPersistenceManager.getNewFeaturesSeen(newFeatureVersion) &&
                featureFlagUseCase.isVerificationPolicySelectionEnabled()

    }

    private fun introductionIsNotFinished() =
        !introductionPersistenceManager.getIntroductionFinished()

}
