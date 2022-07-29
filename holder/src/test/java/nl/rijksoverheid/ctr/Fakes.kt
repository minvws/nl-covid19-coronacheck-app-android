package nl.rijksoverheid.ctr.holder

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.models.ServerTime
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ClockDeviationUseCase
import nl.rijksoverheid.ctr.holder.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.api.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.api.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.dashboard.DashboardViewModel
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.holder.get_events.models.*
import nl.rijksoverheid.ctr.holder.get_events.usecases.ConfigProvidersUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.EventProvidersResult
import nl.rijksoverheid.ctr.holder.get_events.usecases.TestProvidersResult
import nl.rijksoverheid.ctr.holder.input_token.utils.TokenValidatorUtil
import nl.rijksoverheid.ctr.holder.qrcodes.models.QrCodeFragmentData
import nl.rijksoverheid.ctr.holder.qrcodes.models.ReadEuropeanCredentialUtil
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.utils.QrCodeUtil
import nl.rijksoverheid.ctr.holder.your_events.YourEventsViewModel
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.introduction.setup.SetupViewModel
import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.*
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.persistence.database.usecases.*
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset

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
            appStatusLiveData.value = appStatus
        }

        override fun saveNewFeaturesFinished() {

        }

        override fun saveNewTerms() {

        }
    }

fun fakeDashboardViewModel(tabItems: List<DashboardTabItem> = listOf(fakeDashboardTabItem)) =
    object : DashboardViewModel() {
        override fun refresh(dashboardSync: DashboardSync) {
            (dashboardTabItemsLiveData as MutableLiveData<List<DashboardTabItem>>)
                .postValue(tabItems)
        }

        override fun removeOrigin(originEntity: OriginEntity) {

        }

        override fun dismissPolicyInfo(disclosurePolicy: DisclosurePolicy) {

        }
    }

fun fakeRemoveExpiredEventsUseCase() = object : RemoveExpiredEventsUseCase {
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

fun fakeCachedAppConfigUseCase(
    appConfig: HolderConfig = HolderConfig.default(),
): HolderCachedAppConfigUseCase = object : HolderCachedAppConfigUseCase {
    override fun getCachedAppConfig(): HolderConfig {
        return appConfig
    }

    override fun getCachedAppConfigOrNull(): HolderConfig? {
        return appConfig
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

fun fakeSetupViewModel(updateConfig: Boolean = true): SetupViewModel {
    return object : SetupViewModel() {
        override fun onConfigUpdated() {
            if (updateConfig) {
                (introductionDataLiveData as MutableLiveData).postValue(IntroductionData())
            }
        }
    }
}

fun fakeTestProviderRepository(
    model: SignedResponseWithModel<RemoteProtocol> = SignedResponseWithModel(
        rawResponse = "dummy".toByteArray(),
        model = RemoteProtocol(
            protocolVersion = "1",
            providerIdentifier = "1",
            status = RemoteProtocol.Status.COMPLETE,
            holder = null,
            events = null
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
            signingCertificateBytes: List<ByteArray>,
            tlsCertificateBytes: List<ByteArray>,
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

        override suspend fun eventProvidersBES(): EventProvidersResult {
            return EventProvidersResult.Success(eventProviders)
        }

        override suspend fun testProviders(): TestProvidersResult {
            return TestProvidersResult.Success(testProviders)
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

        override fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String {
            return ""
        }

        override fun disclose(
            secretKey: ByteArray,
            credential: ByteArray,
            currentTimeMillis: Long,
            disclosurePolicy: GreenCardDisclosurePolicy
        ): String {
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
                        category = "2"
                    ),
                )
            )
        }

        override fun readEuropeanCredential(credential: ByteArray): JSONObject {
            val jsonObject = JSONObject()
            val vaccinationJson = JSONObject()
            vaccinationJson.put("dn", "1")
            vaccinationJson.put("sd", "1")
            vaccinationJson.put("dt", "2021-01-22")
            val dccValues = JSONArray()
            dccValues.put(0, vaccinationJson)
            dccValues.put(1, vaccinationJson)
            val dccJson = JSONObject()
            dccJson.put("v", dccValues)
            jsonObject.put("dcc", dccJson)
            return jsonObject
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
                "1",
                "",
                "",
                "",
                "",
                "24",
                "1622731645",
                "2"
            )
        }
    }
}

fun fakeEventProviderRepository(
    unomi: ((url: String) -> NetworkRequestResult<RemoteUnomi>) = {
        NetworkRequestResult.Success(
            RemoteUnomi("", "", false)
        )
    },
    events: ((url: String) -> NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>>) = {
        NetworkRequestResult.Success(
            SignedResponseWithModel(
                "".toByteArray(),
                RemoteProtocol(
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
        scope: String?,
        signingCertificateBytes: List<ByteArray>,
        provider: String,
        tlsCertificateBytes: List<ByteArray>,
    ): NetworkRequestResult<RemoteUnomi> {
        return unomi.invoke(url)
    }

    override suspend fun getEvents(
        url: String,
        token: String,
        signingCertificateBytes: List<ByteArray>,
        filter: String,
        scope: String?,
        provider: String,
        tlsCertificateBytes: List<ByteArray>,
    ): NetworkRequestResult<SignedResponseWithModel<RemoteProtocol>> {
        return events.invoke(url)
    }
}

fun fakeGreenCardUtil(
    isExpired: Boolean = false,
    expireDate: OffsetDateTime = OffsetDateTime.now(),
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
    isExpiring: Boolean = false,
    hasNoActiveCredentials: Boolean = false
) = object : GreenCardUtil {
    override suspend fun getAllGreenCards(): List<GreenCard> {
        return listOf()
    }

    override fun hasOrigin(greenCards: List<GreenCard>, originType: OriginType): Boolean {
        return true
    }

    override fun isExpired(greenCard: GreenCard): Boolean {
        return isExpired
    }

    override fun getExpireDate(greenCard: GreenCard, type: OriginType?): OffsetDateTime {
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

    override fun isDomesticTestGreenCard(greenCard: GreenCard): Boolean {
        return true
    }

    override fun isForeignDcc(greenCard: GreenCard): Boolean {
        return false
    }
}

fun fakeGetRemoteGreenCardUseCase(
    result: RemoteGreenCardsResult = RemoteGreenCardsResult.Success(
        RemoteGreenCards(null, null, listOf(), listOf())
    )
) = object : GetRemoteGreenCardsUseCase {
    override suspend fun get(events: List<EventGroupEntity>, secretKey: String, flow: Flow): RemoteGreenCardsResult {
        return result
    }
}

fun fakeSyncRemoteGreenCardUseCase(
    result: SyncRemoteGreenCardsResult = SyncRemoteGreenCardsResult.Success,
) = object : SyncRemoteGreenCardsUseCase {
    override suspend fun execute(remoteGreenCards: RemoteGreenCards, secretKey: String): SyncRemoteGreenCardsResult {
        return result
    }
}

fun fakeReadEuropeanCredentialUtil(dosis: String = "") = object : ReadEuropeanCredentialUtil {
    override fun getDose(readEuropeanCredential: JSONObject): String {
        return dosis
    }

    override fun getOfTotalDoses(readEuropeanCredential: JSONObject): String {
        return "2"
    }

    override fun getDoseRangeStringForVaccination(readEuropeanCredential: JSONObject): String {
        return ""
    }

    override fun doseExceedsTotalDoses(readEuropeanCredential: JSONObject): Boolean {
        return false
    }
}

fun fakeQrCodeUsecase() = object : QrCodeUseCase {
    override suspend fun qrCode(
        credential: ByteArray,
        shouldDisclose: QrCodeFragmentData.ShouldDisclose,
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

fun fakeRemoteEventVaccination(date: LocalDate = LocalDate.now()) =
    RemoteEventVaccination.Vaccination(
        date = date,
        hpkCode = "",
        type = "",
        brand = "",
        completedByMedicalStatement = false,
        completedByPersonalStatement = false,
        completionReason = "",
        doseNumber = "",
        totalDoses = "",
        country = "",
        manufacturer = ""
    )

fun fakeGreenCard(
    greenCardType: GreenCardType = GreenCardType.Domestic,
    originType: OriginType = OriginType.Vaccination,
    eventTime: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    category: String? = null,
    credentialEntities: List<CredentialEntity> = listOf(
        CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 0,
            validFrom = validFrom,
            expirationTime = expirationTime,
            category = category
        )
    ),
) = GreenCard(
    greenCardEntity = GreenCardEntity(
        id = 0,
        walletId = 0,
        type = greenCardType
    ),
    origins = listOf(
        OriginEntity(
            id = 0,
            greenCardId = 0,
            type = originType,
            eventTime = eventTime,
            expirationTime = expirationTime,
            validFrom = validFrom
        )
    ),
    credentialEntities = credentialEntities
)

fun fakeGreenCardWithOrigins(
    greenCardType: GreenCardType = GreenCardType.Domestic,
    originTypes: List<OriginType> = listOf(OriginType.Vaccination),
    eventTime: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    category: String? = null
) = GreenCard(
    greenCardEntity = GreenCardEntity(
        id = 0,
        walletId = 0,
        type = greenCardType
    ),
    origins = originTypes.map {
        OriginEntity(
            id = 0,
            greenCardId = 0,
            type = it,
            eventTime = eventTime,
            expirationTime = expirationTime,
            validFrom = validFrom
        )
    },
    credentialEntities = listOf(
        CredentialEntity(
            id = 0,
            greenCardId = 0,
            data = "".toByteArray(),
            credentialVersion = 0,
            validFrom = validFrom,
            expirationTime = expirationTime,
            category = category
        )
    )
)

val fakeDomesticVaccinationGreenCard = fakeGreenCard(GreenCardType.Eu, OriginType.Vaccination)
val fakeDomesticTestGreenCard = fakeGreenCard(GreenCardType.Eu, OriginType.Test)

val fakeEuropeanVaccinationGreenCard = fakeGreenCard(GreenCardType.Eu, OriginType.Vaccination)
val fakeEuropeanVaccinationTestCard = fakeGreenCard(GreenCardType.Eu, OriginType.Test)

fun fakeAppConfigFreshnessUseCase(shouldShowWarning: Boolean = false) = object :
    AppConfigFreshnessUseCase {
    override fun getAppConfigLastFetchedSeconds(): Long {
        return 0
    }

    override fun getAppConfigMaxValidityTimestamp(): Long {
        return 0
    }

    override fun shouldShowConfigFreshnessWarning(): Boolean {
        return shouldShowWarning
    }
}

val fakeDashboardTabItem = DashboardTabItem(
    title = R.string.travel_button_domestic,
    greenCardType = GreenCardType.Domestic,
    items = listOf()
)

fun fakeEventGroupEntity(
    id: Int = 0,
    walletId: Int = 1,
    providerIdentifier: String = "",
    type: OriginType = OriginType.Vaccination,
    scope: String = "",
    maxIssuedAt: OffsetDateTime = OffsetDateTime.of(
        2000, 1, 1, 1, 1, 1, 1, ZoneOffset.ofTotalSeconds(0)
    ),
    jsonData: ByteArray = ByteArray(1)
) = EventGroupEntity(id, walletId, providerIdentifier, type, scope, maxIssuedAt, jsonData)

fun fakeRemoteGreenCards(
    domesticGreencard: RemoteGreenCards.DomesticGreenCard? = fakeDomesticGreenCard(),
    euGreencards: List<RemoteGreenCards.EuGreenCard>? = listOf(fakeEuGreenCard())
) = RemoteGreenCards(domesticGreencard, euGreencards, listOf(), listOf())

fun fakeDomesticGreenCard(
    origins: List<RemoteGreenCards.Origin> = listOf(fakeOrigin()),
    createCredentialMessages: ByteArray = ByteArray(1)
) = RemoteGreenCards.DomesticGreenCard(origins, createCredentialMessages)

fun fakeEuGreenCard(
    origins: List<RemoteGreenCards.Origin> = listOf(fakeOrigin()),
    credential: String = "credential"
) = RemoteGreenCards.EuGreenCard(origins, credential)

fun fakeOrigin(
    type: OriginType = OriginType.Vaccination,
    eventTime: OffsetDateTime = OffsetDateTime.of(
        2000, 1, 1, 1, 1, 1, 1, ZoneOffset.ofTotalSeconds(0)
    ),
    expirationTime: OffsetDateTime = OffsetDateTime.of(
        2000, 1, 1, 1, 1, 1, 1, ZoneOffset.ofTotalSeconds(0)
    ),
    validFrom: OffsetDateTime = OffsetDateTime.of(
        2000, 1, 1, 1, 1, 1, 1, ZoneOffset.ofTotalSeconds(0)
    ),
    doseNumber: Int? = 1
) = RemoteGreenCards.Origin(type, eventTime, expirationTime, validFrom, doseNumber, listOf())

fun fakeOriginEntity(
    id: Int = 0,
    greenCardId: Long = 1L,
    type: OriginType = OriginType.Vaccination,
    eventTime: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    doseNumber: Int? = null
) = OriginEntity(id, greenCardId, type, eventTime, expirationTime, validFrom, doseNumber)

fun fakeCardsItems(originTypes: List<OriginType>): List<DashboardItem.CardsItem> {
    return originTypes.map {
        fakeCardsItem(
            originType = it
        )
    }
}

fun fakeCardsItem(
    originType: OriginType = OriginType.Vaccination,
    greenCard: GreenCard = fakeGreenCard(originType = originType)
): DashboardItem.CardsItem {
    return DashboardItem.CardsItem(
        cards = listOf(
            DashboardItem.CardsItem.CardItem(
                greenCard = greenCard,
                originStates = listOf(OriginState.Valid(fakeOriginEntity(type = originType))),
                credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                databaseSyncerResult = DatabaseSyncerResult.Success(),
                disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                greenCardEnabledState = GreenCardEnabledState.Enabled
            )
        )
    )
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
    domesticQRRefreshSeconds: Int = 10,
    maxValidityHours: Int = 0
) = HolderConfig.default(
    holderMinimumVersion = minimumVersion,
    holderAppDeactivated = appDeactivated,
    holderInformationURL = informationURL,
    configTTL = configTtlSeconds,
    maxValidityHours = maxValidityHours,
    euLaunchDate = "",
    credentialRenewalDays = 0,
    domesticCredentialValidity = 0,
    domesticQRRefreshSeconds = domesticQRRefreshSeconds,
    testEventValidityHours = 0,
    recoveryEventValidityDays = 0,
    temporarilyDisabled = false,
    requireUpdateBefore = 0,
    ggdEnabled = true
)

fun fakeQrCodeUtil() = object: QrCodeUtil {
    override fun createQrCode(
        qrCodeContent: String,
        width: Int,
        height: Int,
        errorCorrectionLevel: ErrorCorrectionLevel
    ) = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.RGB_565
    )
}

fun fakeYourEventsViewModel(
    databaseSyncerResult: DatabaseSyncerResult
): YourEventsViewModel {
    return object: YourEventsViewModel() {
        init {
            (yourEventsResult as MutableLiveData).value = Event(databaseSyncerResult)
        }

        override fun saveRemoteProtocolEvents(
            flow: Flow,
            remoteProtocols: Map<RemoteProtocol, ByteArray>,
            removePreviousEvents: Boolean
        ) {

        }

        override fun checkForConflictingEvents(remoteProtocols: Map<RemoteProtocol, ByteArray>) {

        }
    }
}


