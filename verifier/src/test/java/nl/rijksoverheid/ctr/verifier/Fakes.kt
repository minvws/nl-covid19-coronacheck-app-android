package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.VerifierConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.setup.SetupViewModel
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationResult
import nl.rijksoverheid.ctr.shared.models.VerificationResultDetails
import nl.rijksoverheid.ctr.verifier.scanner.models.VerifiedQrResultState
import nl.rijksoverheid.ctr.verifier.scanner.usecases.TestResultValidUseCase
import nl.rijksoverheid.ctr.verifier.scanner.usecases.VerifyQrUseCase
import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

fun fakeAppConfigViewModel(appStatus: AppStatus = AppStatus.NoActionRequired) =
    object : AppConfigViewModel() {
        override fun refresh(mobileCoreWrapper: MobileCoreWrapper, force: Boolean) {
            if (appStatusLiveData.value != appStatus) {
                appStatusLiveData.postValue(appStatus)
            }
        }

        override fun saveNewFeaturesFinished() {
        }

        override fun saveNewTerms() {
        }
    }

fun fakeIntroductionViewModel(
    introductionRequired: Boolean = true
): IntroductionViewModel {
    return object : IntroductionViewModel() {

        init {
            if (introductionRequired) {
                (introductionRequiredLiveData as MutableLiveData)
                    .postValue(Event(Unit))
            }
        }

        override fun getIntroductionRequired(): Boolean {
            return true
        }

        override fun saveIntroductionFinished(introductionData: IntroductionData) {
        }
    }
}

fun fakeSetupViewModel(): SetupViewModel {
    return object : SetupViewModel() {
        override fun onConfigUpdated() {
        }
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

fun fakeVerifiedQr(
    error: Boolean = false,
    isNLDCC: Boolean = false,
    isSpecimen: String = "0",
    birthDay: String = "dummy",
    birthMonth: String = "dummy",
    firstNameInitial: String = "dummy",
    lastNameInitial: String = "dummy"
) = VerificationResult(
    status = when {
        error -> Mobilecore.VERIFICATION_FAILED_ERROR
        isNLDCC -> Mobilecore.VERIFICATION_FAILED_IS_NL_DCC
        else -> Mobilecore.VERIFICATION_SUCCESS
    },
    details = VerificationResultDetails(birthDay, birthMonth, firstNameInitial, lastNameInitial, isSpecimen, "2", ""),
    error = if (error) {
        "error"
    } else {
        ""
    }
)

fun fakeVerifyQrUseCase(
    isNLDCC: Boolean = false,
    isSpecimen: String = "0",
    error: Boolean = false,
    result: VerifyQrUseCase.VerifyQrResult = VerifyQrUseCase.VerifyQrResult.Success(
        fakeVerifiedQr(isSpecimen = isSpecimen, isNLDCC = isNLDCC, error = error)
    )
) = object : VerifyQrUseCase {
    override suspend fun get(content: String): VerifyQrUseCase.VerifyQrResult {
        return result
    }
}

fun fakeCachedAppConfigUseCase(
    appConfig: AppConfig = VerifierConfig.default()
): CachedAppConfigUseCase = object : CachedAppConfigUseCase {
    override fun isCachedAppConfigValid(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCachedAppConfig(): AppConfig {
        return appConfig
    }

    override fun getCachedAppConfigOrNull(): AppConfig? {
        return appConfig
    }

    override fun getCachedAppConfigHash(): String {
        return ""
    }
}

fun fakeMobileCoreWrapper(): MobileCoreWrapper {
    return object : MobileCoreWrapper {
        override fun createCredentials(body: ByteArray): String {
            return ""
        }

        override fun readCredential(credentials: ByteArray): ByteArray {
            return ByteArray(0)
        }

        override fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String {
            return ""
        }

        override fun disclose(secretKey: ByteArray, credential: ByteArray, currentTimeMillis: Long, disclosurePolicy: GreenCardDisclosurePolicy): String {
            return ""
        }

        override fun generateHolderSk(): String {
            return ""
        }

        override fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential> {
            return listOf()
        }

        override fun readEuropeanCredential(credential: ByteArray): JSONObject {
            return JSONObject()
        }

        override fun initializeHolder(configFilesPath: String): String? = null

        override fun initializeVerifier(configFilesPath: String) = ""

        override fun verify(credential: ByteArray, policy: VerificationPolicy): VerificationResult {
            TODO("Not yet implemented")
        }

        override fun isDcc(credential: ByteArray): Boolean {
            return false
        }

        override fun isForeignDcc(credential: ByteArray): Boolean {
            return false
        }

        override fun hasDomesticPrefix(credential: ByteArray): Boolean {
            return false
        }

        override fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential {
            return ReadDomesticCredential(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "2"
            )
        }

        override fun isPaperBasedDCC(credential: ByteArray): Boolean {
            return false
        }
    }
}

fun fakeAppConfigPersistenceManager(
    lastFetchedSeconds: Long = 0L
) = object : AppConfigPersistenceManager {

    override fun getAppConfigLastFetchedSeconds(): Long {
        return lastFetchedSeconds
    }

    override fun saveAppConfigLastFetchedSeconds(seconds: Long) {
    }
}

fun fakeAppConfig(
    minimumVersion: Int = 1,
    appDeactivated: Boolean = false,
    informationURL: String = "",
    configTtlSeconds: Int = 0,
    maxValidityHours: Int = 0
) = VerifierConfig.default(
    verifierMinimumVersion = minimumVersion,
    verifierAppDeactivated = appDeactivated,
    verifierInformationURL = informationURL,
    configTTL = configTtlSeconds,
    maxValidityHours = maxValidityHours
)
