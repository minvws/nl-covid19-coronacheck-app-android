package nl.rijksoverheid.ctr.holder

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.api.model.PublicKeys
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.LocalTestResultViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.LocalTestResultUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.new_terms.models.NewTerms
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import nl.rijksoverheid.ctr.shared.models.TestResultAttributes
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
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

fun fakeTokenValidatorUtil(
    isValid: Boolean = true
) = object : TokenValidatorUtil {
    override fun validate(token: String, checksum: String): Boolean {
        return isValid
    }
}

fun fakeTestResultUtil(
    isValid: Boolean = true
) = object : TestResultUtil {
    override fun isValid(sampleDate: OffsetDateTime, validitySeconds: Long): Boolean {
        return isValid
    }
}

fun fakePersonalDetailsUtil(

): PersonalDetailsUtil = object : PersonalDetailsUtil {
    override fun getPersonalDetails(
        firstNameInitial: String,
        lastNameInitial: String,
        birthDay: String,
        birthMonth: String,
        includeBirthMonthNumber: Boolean
    ): PersonalDetails {
        return PersonalDetails(
            firstNameInitial = firstNameInitial,
            lastNameInitial = lastNameInitial,
            birthDay = birthDay,
            birthMonth = birthMonth
        )
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

fun fakeQrCodeUseCase(
    bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
): QrCodeUseCase {
    return object : QrCodeUseCase {
        override suspend fun qrCode(
            credentials: ByteArray,
            qrCodeWidth: Int,
            qrCodeHeight: Int
        ): Bitmap {
            return bitmap
        }
    }
}

fun fakeIntroductionViewModel(
    introductionStatus: IntroductionStatus = IntroductionStatus.IntroductionFinished.NoActionRequired,
): IntroductionViewModel {
    return object : IntroductionViewModel() {
        override fun getIntroductionStatus(): IntroductionStatus {
            return introductionStatus
        }

        override fun saveIntroductionFinished(newTerms: NewTerms?) {

        }

    }
}

fun fakeLocalTestResultViewModel(
    localTestResultState: LocalTestResultState = LocalTestResultState.None,
): LocalTestResultViewModel {
    return object : LocalTestResultViewModel() {
        override fun getLocalTestResult() {
            localTestResultStateLiveData.value = Event(localTestResultState)
        }

        override fun generateQrCode(size: Int): Boolean {
            if (localTestResultState is LocalTestResultState.Valid) {
                qrCodeLiveData.value =
                    QrCodeData(
                        localTestResult = LocalTestResult(
                            credentials = "dummy",
                            sampleDate = OffsetDateTime.now(),
                            expireDate = OffsetDateTime.now(),
                            testType = "dummy",
                            personalDetails = PersonalDetails("X", "X", "X", "X")
                        ),
                        qrCode = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                    )
                return true
            } else {
                return false
            }
        }
    }
}

fun fakeLocalTestResultUseCase(
    state: LocalTestResultState = LocalTestResultState.None
): LocalTestResultUseCase {
    return object : LocalTestResultUseCase {
        override suspend fun get(currentLocalTestResultState: LocalTestResultState?): LocalTestResultState {
            return state
        }
    }
}

fun fakeCreateCredentialUseCase(
    credential: String = ""
): CreateCredentialUseCase {
    return object : CreateCredentialUseCase {
        override fun get(secretKeyJson: String, testIsmBody: String): String {
            return credential
        }
    }
}

fun fakeSecretKeyUseCase(
    json: String = "{}"
): SecretKeyUseCase {
    return object : SecretKeyUseCase {
        override fun json(): String {
            return json
        }

        override fun persist() {

        }
    }
}

fun fakeCommitmentMessageUsecase(
    json: String = "{}"
): CommitmentMessageUseCase {
    return object : CommitmentMessageUseCase {
        override suspend fun json(nonce: String): String {
            return json
        }
    }
}

fun fakeTestProviderRepository(
    model: SignedResponseWithModel<RemoteTestResult> = SignedResponseWithModel(
        rawResponse = "dummy".toByteArray(),
        model = RemoteTestResult(
            result = null,
            protocolVersion = "1",
            providerIdentifier = "1",
            status = RemoteTestResult.Status.COMPLETE
        ),
    ),
    remoteTestResultExceptionCallback: (() -> Unit)? = null,
): TestProviderRepository {
    return object : TestProviderRepository {
        override suspend fun remoteTestResult(
            url: String,
            token: String,
            verifierCode: String?,
            signingCertificateBytes: ByteArray
        ): SignedResponseWithModel<RemoteTestResult> {
            remoteTestResultExceptionCallback?.invoke()
            return model
        }
    }
}

fun fakeTestProviderUseCase(
    provider: RemoteConfigProviders.TestProvider? = null
): TestProviderUseCase {
    return object : TestProviderUseCase {
        override suspend fun testProvider(id: String): RemoteConfigProviders.TestProvider? {
            return provider
        }
    }
}

fun fakeCoronaCheckRepository(
    testProviders: RemoteConfigProviders = RemoteConfigProviders(listOf()),
    testIsmResult: TestIsmResult = TestIsmResult.Success(""),
    testIsmExceptionCallback: (() -> Unit)? = null,
    remoteNonce: RemoteNonce = RemoteNonce("", ""),

    ): CoronaCheckRepository {
    return object : CoronaCheckRepository {
        override suspend fun testProviders(): RemoteConfigProviders {
            return testProviders
        }

        override suspend fun getTestIsm(test: String, sToken: String, icm: String): TestIsmResult {
            testIsmExceptionCallback?.invoke()
            return testIsmResult
        }

        override suspend fun remoteNonce(): RemoteNonce {
            return remoteNonce
        }
    }
}

fun fakeTestResultAttributesUseCase(
    sampleTimeSeconds: Long = 0L,
    testType: String = "",
    birthDay: String = "",
    birthMonth: String = "",
    firstNameInitial: String = "",
    lastNameInitial: String = "",
    isSpecimen: String = "0",
    isPaperProof: String = "0"
): TestResultAttributesUseCase {
    return object : TestResultAttributesUseCase {
        override fun get(credentials: String): TestResultAttributes {
            return TestResultAttributes(
                sampleTime = sampleTimeSeconds,
                testType = testType,
                birthDay = birthDay,
                birthMonth = birthMonth,
                firstNameInitial = firstNameInitial,
                lastNameInitial = lastNameInitial,
                isSpecimen = isSpecimen,
                isPaperProof = isPaperProof
            )
        }
    }
}

fun fakePersistenceManager(
    secretKeyJson: String? = "",
    credentials: String? = "",
    hasSeenCameraRationale: Boolean? = false
): PersistenceManager {
    return object : PersistenceManager {
        override fun saveSecretKeyJson(json: String) {

        }

        override fun getSecretKeyJson(): String? {
            return secretKeyJson
        }

        override fun saveCredentials(credentials: String) {

        }

        override fun getCredentials(): String? {
            return credentials
        }

        override fun deleteCredentials() {

        }

        override fun hasSeenCameraRationale(): Boolean? {
            return hasSeenCameraRationale
        }

        override fun setHasSeenCameraRationale(hasSeen: Boolean) {

        }

        override fun hasDismissedRootedDeviceDialog(): Boolean {
            return false
        }

        override fun setHasDismissedRootedDeviceDialog() {
            
        }
    }
}


