package nl.rijksoverheid.ctr.appconfig.usecases

interface FeatureFlagUseCase {
    fun isVerificationPolicyEnabled(appBuildVersion: Int): Boolean
}

class FeatureFlagUseCaseImpl(
    private val appConfigUseCase: CachedAppConfigUseCase
): FeatureFlagUseCase {

    override fun isVerificationPolicyEnabled(appBuildVersion: Int): Boolean {
        return appBuildVersion >= appConfigUseCase.getCachedAppConfig().enableVerificationPolicyVersion
    }
}