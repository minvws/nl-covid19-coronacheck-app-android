package nl.rijksoverheid.ctr.verifier

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
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

fun fakeIntroductionViewModel(
    introductionFinished: Boolean
): IntroductionViewModel {
    return object : IntroductionViewModel() {
        override fun introductionFinished(): Boolean {
            return introductionFinished
        }

        override fun saveIntroductionFinished() {

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
    result: TestResultValidUseCase.TestResultValidResult = TestResultValidUseCase.TestResultValidResult.Valid(
        verifiedQr = fakeVerifiedQr
    )
) = object : TestResultValidUseCase {
    override suspend fun validate(qrContent: String): TestResultValidUseCase.TestResultValidResult {
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

val fakeVerifiedQr = VerifiedQr(
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



