package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.model.AppStatus
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.models.NewTerms
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.introduction.privacy_consent.models.PrivacyPolicyItem
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.util.TestResultUtil
import nl.rijksoverheid.ctr.verifier.models.VerifiedQr
import nl.rijksoverheid.ctr.verifier.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.usecases.VerifyQrUseCase
import nl.rijksoverheid.ctr.verifier.util.QrCodeUtil
import java.time.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeAppConfigViewModel(appStatus: AppStatus = AppStatus.NoActionRequired) =
    object : AppConfigViewModel() {
        override fun refresh() {
            appStatusLiveData.value = appStatus
        }
    }

fun fakeIntroductionViewModel(
    introductionStatus: IntroductionStatus = IntroductionStatus.IntroductionFinished.NoActionRequired,
): IntroductionViewModel {
    return object : IntroductionViewModel() {
        override fun getIntroductionStatus(
            onboardingItems: List<OnboardingItem>,
            privacyPolicyItems: List<PrivacyPolicyItem>,
            newTerms: NewTerms?
        ): IntroductionStatus {
            return introductionStatus
        }

        override fun getIntroductionFinished(): Boolean {
            return introductionStatus is IntroductionStatus.IntroductionFinished.NoActionRequired || introductionStatus is IntroductionStatus.IntroductionFinished.ConsentNeeded
        }

        override fun saveIntroductionFinished(newTerms: NewTerms?) {

        }
    }
}

fun fakeScanQrViewModel(
    result: VerifiedQrResultState,
    scanInstructionsSeen: Boolean
) = object : ScanQrViewModel() {
    override fun validate(qrContent: String) {
        validatedQrLiveData.value = Event(result)
    }

    override fun scanInstructionsSeen(): Boolean {
        return scanInstructionsSeen
    }
}

fun fakeTestResultValidUseCase(
    result: VerifiedQrResultState = VerifiedQrResultState.Valid(
        verifiedQr = fakeVerifiedQr()
    )
) = object : TestResultValidUseCase {
    override suspend fun validate(qrContent: String): VerifiedQrResultState {
        return result
    }
}

fun fakeQrCodeUtil(
    isValid: Boolean = true
) = object : QrCodeUtil {
    override fun isValid(creationDate: OffsetDateTime, isPaperProof: String): Boolean {
        return isValid
    }
}

fun fakeVerifiedQr(
    isSpecimen: String = "0",
    birthDay: String = "dummy",
    birthMonth: String = "dummy",
    firstNameInitial: String = "dummy",
    lastNameInitial: String = "dummy"
) = VerifiedQr(
    creationDateSeconds = 0,
    testResultAttributes = TestResultAttributes(
        sampleTime = 0,
        testType = "dummy",
        birthDay = birthDay,
        birthMonth = birthMonth,
        firstNameInitial = firstNameInitial,
        lastNameInitial = lastNameInitial,
        isPaperProof = "0",
        isSpecimen = isSpecimen
    )
)

fun fakeVerifyQrUseCase(
    result: VerifyQrUseCase.VerifyQrResult = VerifyQrUseCase.VerifyQrResult.Success(
        verifiedQr = VerifiedQr(
            creationDateSeconds = 0,
            testResultAttributes = TestResultAttributes(
                sampleTime = 0,
                testType = "dummy",
                birthDay = "dummy",
                birthMonth = "dummy",
                firstNameInitial = "dummy",
                lastNameInitial = "dummy",
                isPaperProof = "0",
                isSpecimen = "0"
            )
        )
    )
) = object : VerifyQrUseCase {
    override suspend fun get(content: String): VerifyQrUseCase.VerifyQrResult {
        return result
    }
}

fun fakeTestResultUtil(
    isValid: Boolean = true
) = object : TestResultUtil {
    override fun isValid(sampleDate: OffsetDateTime, validitySeconds: Long): Boolean {
        return isValid
    }
}

fun fakeCachedAppConfigUseCase(
    appConfig: AppConfig = AppConfig(
        minimumVersion = 0,
        appDeactivated = false,
        informationURL = "dummy",
        configTtlSeconds = 0,
        maxValidityHours = 0
    ),
    publicKeys: PublicKeys = PublicKeys(
        clKeys = listOf()
    )
): CachedAppConfigUseCase = object : CachedAppConfigUseCase {
    override fun persistAppConfig(appConfig: AppConfig) {

    }

    override fun getCachedAppConfig(): AppConfig {
        return appConfig
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return appConfig.maxValidityHours
    }

    override fun persistPublicKeys(publicKeys: PublicKeys) {

    }

    override fun getCachedPublicKeys(): PublicKeys? {
        return publicKeys
    }
}



