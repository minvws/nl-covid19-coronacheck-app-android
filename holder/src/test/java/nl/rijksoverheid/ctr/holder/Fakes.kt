package nl.rijksoverheid.ctr.holder

import androidx.lifecycle.LiveData
import mobilecore.Result
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.CommercialTestCodeViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.MyOverviewViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.*
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.BufferedSource
import org.json.JSONObject
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
        override fun refresh(mobileCoreWrapper: MobileCoreWrapper) {
            appStatusLiveData.value = appStatus
        }
    }

fun fakeMyOverViewModel() =
    object : MyOverviewViewModel() {

        override fun getSelectedType(): GreenCardType {
            return GreenCardType.Domestic
        }

        override fun refreshOverviewItems(selectType: GreenCardType, syncDatabase: Boolean) {

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
        maxValidityHours = 0,
        euLaunchDate = "",
        credentialRenewalDays = 0,
        domesticCredentialValidity = 0,
        testEventValidity = 0,
        recoveryEventValidity = 0,
        temporarilyDisabled = false,
        requireUpdateBefore = 0
    ),
    publicKeys: BufferedSource = "{\"cl_keys\":[]}".toResponseBody("application/json".toMediaType())
        .source()
): CachedAppConfigUseCase = object : CachedAppConfigUseCase {
    override fun getCachedAppConfig(): AppConfig {
        return appConfig
    }

    override fun getCachedAppConfigRecoveryEventValidity(): Int {
        return appConfig.recoveryEventValidity
    }

    override fun getCachedAppConfigMaxValidityHours(): Int {
        return appConfig.maxValidityHours
    }

    override fun getCachedAppConfigVaccinationEventValidity(): Int {
        return appConfig.vaccinationEventValidity
    }

    override fun getCachedPublicKeys() = publicKeys

    override fun getProviderName(providerIdentifier: String?): String {
        return ""
    }
}

fun fakeIntroductionViewModel(
    introductionStatus: IntroductionStatus = IntroductionStatus.IntroductionFinished.NoActionRequired,
): IntroductionViewModel {
    return object : IntroductionViewModel() {
        override fun getIntroductionStatus(): IntroductionStatus {
            return introductionStatus
        }

        override fun saveIntroductionFinished(introductionData: IntroductionData) {

        }

        override fun saveNewFeaturesFinished(newFeaturesVersion: Int) {

        }

    }
}

fun fakeCommercialTestResultViewModel(): CommercialTestCodeViewModel {
    return object : CommercialTestCodeViewModel() {

        override fun updateViewState() {

        }

        override fun getTestResult(fromDeeplink: Boolean) {

        }

        override fun sendVerificationCode() {

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
    model: SignedResponseWithModel<RemoteProtocol> = SignedResponseWithModel(
        rawResponse = "dummy".toByteArray(),
        model = RemoteTestResult2(
            result = null,
            protocolVersion = "1",
            providerIdentifier = "1",
            status = RemoteProtocol.Status.COMPLETE
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
        ): SignedResponseWithModel<RemoteProtocol> {
            remoteTestResultExceptionCallback?.invoke()
            return model
        }
    }
}

fun fakeConfigProviderUseCase(
    eventProviders: List<RemoteConfigProviders.EventProvider> = listOf(),
    provider: RemoteConfigProviders.TestProvider? = null
): ConfigProvidersUseCase {
    return object : ConfigProvidersUseCase {
        override suspend fun eventProviders(): List<RemoteConfigProviders.EventProvider> {
            return eventProviders
        }

        override suspend fun testProvider(id: String): RemoteConfigProviders.TestProvider? {
            return provider
        }
    }
}

fun fakeCoronaCheckRepository(
    testProviders: RemoteConfigProviders = RemoteConfigProviders(listOf(), listOf()),
    testIsmResult: TestIsmResult = TestIsmResult.Success(""),
    testIsmExceptionCallback: (() -> Unit)? = null,
    remoteNonce: RemoteNonce = RemoteNonce("", ""),
    accessTokens: RemoteAccessTokens = RemoteAccessTokens(tokens = listOf()),
    remoteCredentials: RemoteCredentials = RemoteCredentials(
        domesticGreencard = null,
        euGreencards = null
    ),
    prepareIssue: RemotePrepareIssue = RemotePrepareIssue(
        stoken = "",
        prepareIssueMessage = "".toByteArray()
    )

): CoronaCheckRepository {
    return object : CoronaCheckRepository {

        override suspend fun configProviders(): RemoteConfigProviders {
            return testProviders
        }

        override suspend fun accessTokens(tvsToken: String): RemoteAccessTokens {
            return accessTokens
        }

        override suspend fun getCredentials(
            stoken: String,
            events: List<String>,
            issueCommitmentMessage: String
        ): RemoteCredentials {
            return remoteCredentials
        }

        override suspend fun getPrepareIssue(): RemotePrepareIssue {
            return prepareIssue
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
                birthDay = birthDay,
                birthMonth = birthMonth,
                firstNameInitial = firstNameInitial,
                lastNameInitial = lastNameInitial,
                isSpecimen = isSpecimen,
                isNLDCC = "1",
                credentialVersion = "1",
                isPaperProof = "0",
                validForHours = "24",
                validFrom = "1622633766",
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

        override fun getSelectedGreenCardType(): GreenCardType {
            return GreenCardType.Domestic
        }

        override fun setSelectedGreenCardType(greenCardType: GreenCardType) {

        }

        override fun hasAppliedJune28Fix(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setJune28FixApplied(applied: Boolean) {
            TODO("Not yet implemented")
        }
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

        override fun createCommitmentMessage(secretKey: ByteArray, nonce: ByteArray): String {
            return ""
        }

        override fun disclose(secretKey: ByteArray, credential: ByteArray): String {
            return ""
        }

        override fun generateHolderSk(): String {
            return ""
        }

        override fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential> {
            return listOf(
                DomesticCredential(
                    credential = JSONObject(),
                    attributes = DomesticCredentialAttributes(
                        birthDay = "",
                        birthMonth = "6",
                        credentialVersion = 2,
                        firstNameInitial = "B",
                        isSpecimen = "0",
                        lastNameInitial = "",
                        isPaperProof = "0",
                        validForHours = 24,
                        validFrom = 1622731645L,
                    ),
                )
            )
        }

        override fun readEuropeanCredential(credential: ByteArray): JSONObject {
            return JSONObject()
        }

        override fun initializeHolder(configFilesPath: String): String? = null

        override fun initializeVerifier(configFilesPath: String) = ""

        override fun verify(credential: ByteArray): Result {
            TODO("Not yet implemented")
        }

        override fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential {
            return ReadDomesticCredential(
                "",
                "",
                "1",
                "",
                "",
                "",
                "",
                "24",
                "1622731645"
            )
        }
    }
}

fun fakeEventProviderRepository(
    unomi: ((url: String) -> RemoteUnomi) = { RemoteUnomi("", "", false) },
    events: ((url: String) -> SignedResponseWithModel<RemoteProtocol3>) = {
        SignedResponseWithModel(
            "".toByteArray(),
            RemoteProtocol3(
                "", "", RemoteProtocol.Status.COMPLETE, null, listOf()
            ),
        )
    },
) = object : EventProviderRepository {
    override suspend fun getUnomi(
        url: String,
        token: String,
        filter: String,
        signingCertificateBytes: ByteArray
    ): RemoteUnomi {
        return unomi.invoke(url)
    }

    override suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String
    ): SignedResponseWithModel<RemoteProtocol3> {
        return events.invoke(url)
    }

}


