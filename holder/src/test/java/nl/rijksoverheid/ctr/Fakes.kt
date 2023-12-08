package nl.rijksoverheid.ctr

import android.net.ConnectivityManager
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.appconfig.persistence.AppConfigPersistenceManager
import nl.rijksoverheid.ctr.appconfig.usecases.AppConfigFreshnessUseCase
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationResult
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.json.JSONArray
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

        override fun refresh(
            mobileCoreWrapper: MobileCoreWrapper,
            force: Boolean,
            afterRefresh: () -> Unit
        ) {
            appStatusLiveData.value = appStatus
        }

        override fun saveNewFeaturesFinished() {
        }

        override fun saveNewTerms() {
        }
    }

fun fakeCachedAppConfigUseCase(
    appConfig: HolderConfig = HolderConfig.default()
): HolderCachedAppConfigUseCase = object : HolderCachedAppConfigUseCase {
    override fun getCachedAppConfig(): HolderConfig {
        return appConfig
    }

    override fun getCachedAppConfigOrNull(): HolderConfig {
        return appConfig
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

        override fun createCommitmentMessage(
            secretKey: ByteArray,
            prepareIssueMessage: ByteArray
        ): String {
            return ""
        }

        override fun generateHolderSk(): String {
            return ""
        }

        override fun readEuropeanCredential(credential: ByteArray): JSONObject {
            val jsonObject = JSONObject()
            val vaccinationJson = JSONObject()
            vaccinationJson.put("dn", 1)
            vaccinationJson.put("sd", 1)
            vaccinationJson.put("dt", "2021-01-22")
            vaccinationJson.put("co", "NL")
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
    }
}

fun fakeGreenCard(
    greenCardType: GreenCardType = GreenCardType.Eu,
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
    )
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
    greenCardType: GreenCardType = GreenCardType.Eu,
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
) = EventGroupEntity(id, walletId, providerIdentifier, type, scope, maxIssuedAt, false, jsonData)

fun fakeOriginEntity(
    id: Int = 0,
    greenCardId: Long = 1L,
    type: OriginType = OriginType.Vaccination,
    eventTime: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    doseNumber: Int? = null
) = OriginEntity(id, greenCardId, type, eventTime, expirationTime, validFrom, doseNumber)


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


fun fakeAndroidUtil(isSmallScreen: Boolean) = object : AndroidUtil {
    override fun isSmallScreen() = isSmallScreen

    override fun getMasterKeyAlias(): String {
        TODO("Not yet implemented")
    }

    override fun isNetworkAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getConnectivityManager(): ConnectivityManager {
        TODO("Not yet implemented")
    }

    override fun generateRandomKey(): String {
        TODO("Not yet implemented")
    }

    override fun getFirstInstallTime(): OffsetDateTime {
        TODO("Not yet implemented")
    }
}
