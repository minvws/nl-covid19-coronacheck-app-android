package nl.rijksoverheid.ctr.holder

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.CommercialTestCodeViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.DashboardViewModel
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.DashboardSync
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.TestResultAttributesUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.VerificationResult
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.*
import nl.rijksoverheid.ctr.shared.utils.PersonalDetailsUtil
import nl.rijksoverheid.ctr.shared.utils.TestResultUtil
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
            appStatusLiveData.value = Event(appStatus)
        }
    }

fun fakeDashboardViewModel() =
    object : DashboardViewModel() {
        override fun refresh(dashboardSync: DashboardSync) {

        }

        override fun removeGreenCard(greenCard: GreenCard) {

        }

        override fun dismissGreenCardsSyncedItem() {

        }
    }

fun fakeRemoveExpiredEventsUseCase() = object: RemoveExpiredEventsUseCase {
    override suspend fun execute(events: List<EventGroupEntity>) {

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
    appConfig: HolderConfig = HolderConfig.default(),
): CachedAppConfigUseCase = object : CachedAppConfigUseCase {
    override fun getCachedAppConfig(): HolderConfig {
        return appConfig
    }
}

fun fakeIntroductionViewModel(
    introductionStatus: IntroductionStatus? = null,
): IntroductionViewModel {
    return object : IntroductionViewModel() {

        init {
            if (introductionStatus != null) {
                (introductionStatusLiveData as MutableLiveData).postValue(Event(introductionStatus))
            }
        }

        override fun getIntroductionStatus(): IntroductionStatus {
            return introductionStatus ?: IntroductionStatus.IntroductionFinished.NoActionRequired
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
            provider: String,
            verifierCode: String?,
            signingCertificateBytes: ByteArray
        ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>> {
            remoteTestResultExceptionCallback?.invoke()
            return NetworkRequestResult.Success(model)
        }
    }
}

fun fakeConfigProviderUseCase(
    eventProviders: List<RemoteConfigProviders.EventProvider> = listOf(),
    testProviders: List<RemoteConfigProviders.TestProvider> = listOf()
): ConfigProvidersUseCase {
    return object : ConfigProvidersUseCase {
        override suspend fun eventProviders(): EventProvidersResult {
            return EventProvidersResult.Success(eventProviders)
        }

        override suspend fun testProviders(): TestProvidersResult {
            return TestProvidersResult.Success(testProviders)
        }
    }
}

fun fakeCoronaCheckRepository(
    testProviders: RemoteConfigProviders = RemoteConfigProviders(listOf(), listOf()),
    testIsmResult: TestIsmResult = TestIsmResult.Success(""),
    testIsmExceptionCallback: (() -> Unit)? = null,
    remoteNonce: RemoteNonce = RemoteNonce("", ""),
    accessTokens: RemoteAccessTokens = RemoteAccessTokens(tokens = listOf()),
    remoteCredentials: RemoteGreenCards = RemoteGreenCards(
        domesticGreencard = null,
        euGreencards = null
    ),
    prepareIssue: RemotePrepareIssue = RemotePrepareIssue(
        stoken = "",
        prepareIssueMessage = "".toByteArray()
    )

): CoronaCheckRepository {
    return object : CoronaCheckRepository {

        override suspend fun configProviders(): NetworkRequestResult<RemoteConfigProviders> {
            return NetworkRequestResult.Success(testProviders)
        }

        override suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens> {
            return NetworkRequestResult.Success(accessTokens)
        }

        override suspend fun getGreenCards(
            stoken: String,
            events: List<String>,
            issueCommitmentMessage: String
        ): NetworkRequestResult<RemoteGreenCards> {
            return NetworkRequestResult.Success(remoteCredentials)
        }

        override suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue> {
            return NetworkRequestResult.Success(prepareIssue)
        }

        override suspend fun getCoupling(
            credential: String,
            couplingCode: String
        ): NetworkRequestResult<RemoteCouplingResponse> {
            return NetworkRequestResult.Success(RemoteCouplingResponse(RemoteCouplingStatus.Accepted))
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
    hasSeenCameraRationale: Boolean? = false,
    hasDismissedUnsecureDeviceDialog: Boolean = true,
    showSyncGreenCardsItem: Boolean = true
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

        override fun getSelectedDashboardTab(): Int {
            return 0
        }

        override fun setSelectedDashboardTab(position: Int) {

        }

        override fun hasAppliedJune28Fix(): Boolean {
            TODO("Not yet implemented")
        }

        override fun setJune28FixApplied(applied: Boolean) {
            TODO("Not yet implemented")
        }

        override fun hasDismissedUnsecureDeviceDialog(): Boolean {
            return false
        }

        override fun setHasDismissedUnsecureDeviceDialog(value: Boolean) {
            
        }

        override fun hasDismissedSyncedGreenCardsItem(): Boolean {
            return hasDismissedUnsecureDeviceDialog
        }

        override fun setHasDismissedSyncedGreenCardsItem(dismissed: Boolean) {

        }

        override fun showSyncGreenCardsItem(): Boolean {
            return showSyncGreenCardsItem
        }

        override fun setShowSyncGreenCardsItem(show: Boolean) {

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

        override fun verify(credential: ByteArray): VerificationResult {
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
    unomi: ((url: String) -> NetworkRequestResult<RemoteUnomi>) = { NetworkRequestResult.Success(RemoteUnomi("", "", false)) },
    events: ((url: String) -> NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>>) = {
        NetworkRequestResult.Success(
            SignedResponseWithModel(
                "".toByteArray(),
                RemoteProtocol3(
                    "", "", RemoteProtocol.Status.COMPLETE, null, listOf()
                ),
            )
        )
    },
) = object : EventProviderRepository {
    override suspend fun getUnomi(
        url: String,
        token: String,
        filter: String,
        signingCertificateBytes: ByteArray,
        provider: String,
    ): NetworkRequestResult<RemoteUnomi> {
        return unomi.invoke(url)
    }

    override suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray,
        filter: String,
        provider: String,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol3>> {
        return events.invoke(url)
    }
}

fun fakeGreenCardUtil(
    isExpired: Boolean = false,
    expireDate: OffsetDateTime = OffsetDateTime.now(),
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
    isExpiring: Boolean = false,
    hasNoActiveCredentials: Boolean = false
) = object: GreenCardUtil {
    override fun isExpired(greenCard: GreenCard): Boolean {
        return isExpired
    }

    override fun getExpireDate(greenCard: GreenCard): OffsetDateTime {
        return expireDate
    }

    override fun getErrorCorrectionLevel(greenCardType: GreenCardType): ErrorCorrectionLevel {
        return errorCorrectionLevel
    }

    override fun isExpiring(renewalDays: Long, greenCard: GreenCard): Boolean {
        return isExpiring
    }

    override fun hasNoActiveCredentials(greenCard: GreenCard): Boolean {
        return hasNoActiveCredentials
    }
}

fun fakeCredentialUtil(activeCredential: CredentialEntity? = null) = object: CredentialUtil {
    override fun getActiveCredential(entities: List<CredentialEntity>): CredentialEntity? {
        return activeCredential
    }

    override fun isExpiring(credentialRenewalDays: Long, credential: CredentialEntity): Boolean {
        return false
    }

    override fun getTestTypeForEuropeanCredentials(entities: List<CredentialEntity>): String {
        return ""
    }

    override fun getVaccinationDosesForEuropeanCredentials(
        entities: List<CredentialEntity>,
        getString: (String, String) -> String
    ): String {
        return ""
    }

    override fun vaccinationShouldBeHidden(readEuropeanCredential: List<JSONObject>, index: Int): Boolean {
        return false
    }
}

fun fakeGetRemoteGreenCardUseCase(result: RemoteGreenCardsResult = RemoteGreenCardsResult.Success(
    RemoteGreenCards(null, null)
)) = object: GetRemoteGreenCardsUseCase {
    override suspend fun get(events: List<EventGroupEntity>): RemoteGreenCardsResult {
        return result
    }
}

fun fakeSyncRemoteGreenCardUseCase(
    result: SyncRemoteGreenCardsResult = SyncRemoteGreenCardsResult.Success,
) = object: SyncRemoteGreenCardsUseCase {
    override suspend fun execute(remoteGreenCards: RemoteGreenCards): SyncRemoteGreenCardsResult {
        return result
    }
}

fun fakeClockDevationUseCase(
    hasDeviation: Boolean = false
) = object: ClockDeviationUseCase() {
    override fun store(serverResponseTimestamp: Long, localReceivedTimestamp: Long) {

    }

    override fun hasDeviation(): Boolean {
        return hasDeviation
    }
}

fun fakeReadEuropeanCredentialUtil(dosis: String = "") = object: ReadEuropeanCredentialUtil {
    override fun getDose(readEuropeanCredential: JSONObject): String {
        return dosis
    }

    override fun getOfTotalDoses(readEuropeanCredential: JSONObject): String {
        return "2"
    }

    override fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String {
        return ""
    }
}

fun fakeQrCodeUsecase() = object: QrCodeUseCase {
    override suspend fun qrCode(
        credential: ByteArray,
        shouldDisclose: Boolean,
        qrCodeWidth: Int,
        qrCodeHeight: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ): Bitmap {
        return mockk()
    }
}

val fakeGreenCardEntity = GreenCardEntity(
    id = 0,
    walletId = 1,
    type = GreenCardType.Domestic
)

val fakeGreenCard = GreenCard(
    greenCardEntity = fakeGreenCardEntity,
    origins = listOf(),
    credentialEntities = listOf()
)



