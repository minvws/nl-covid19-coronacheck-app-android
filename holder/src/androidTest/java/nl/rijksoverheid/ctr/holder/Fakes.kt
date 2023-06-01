/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder

import androidx.lifecycle.MutableLiveData
import java.time.OffsetDateTime
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.dashboard.DashboardViewModel
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardSync
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationResult
import org.json.JSONArray
import org.json.JSONObject

fun fakeGreenCardEntity(
    id: Int = 0,
    walletId: Int = 0,
    type: GreenCardType = GreenCardType.Eu
) = GreenCardEntity(id, walletId, type)

fun fakeOriginEntity(
    id: Int = 0,
    greenCardId: Long = 1L,
    type: OriginType = OriginType.Vaccination,
    eventTime: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    doseNumber: Int? = null
) = OriginEntity(id, greenCardId, type, eventTime, expirationTime, validFrom, doseNumber)

fun fakeCredentialEntity(
    id: Long = 0,
    greenCardId: Long = 1L,
    data: ByteArray = "".toByteArray(),
    credentialVersion: Int = 1,
    validFrom: OffsetDateTime = OffsetDateTime.now(),
    expirationTime: OffsetDateTime = OffsetDateTime.now(),
    category: String? = null
) = CredentialEntity(id, greenCardId, data, credentialVersion, validFrom, expirationTime, category)

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

fun fakeDashboardViewModel(tabItems: List<DashboardTabItem> = listOf(fakeDashboardTabItem)) =
    object : DashboardViewModel() {
        override fun refresh(dashboardSync: DashboardSync) {
            (dashboardTabItemsLiveData as MutableLiveData<List<DashboardTabItem>>)
                .postValue(tabItems)
        }

        override fun removeOrigin(originEntity: OriginEntity) {
        }

        override fun dismissBlockedEventsInfo() {
        }

        override fun dismissFuzzyMatchedEventsInfo() {
        }

        override fun scrollUpdate(canScrollVertically: Boolean, greenCardType: GreenCardType) {
        }

        override fun showMigrationDialog() {
        }

        override fun deleteMigrationData() {
        }
    }

val fakeDashboardTabItem = DashboardTabItem(
    title = R.string.travel_button_domestic,
    greenCardType = GreenCardType.Eu,
    items = listOf()
)

val fakeMobileCoreWrapper = object : MobileCoreWrapper {
    override fun createCredentials(body: ByteArray): String {
        TODO("Not yet implemented")
    }

    override fun readCredential(credentials: ByteArray): ByteArray {
        TODO("Not yet implemented")
    }

    override fun createCommitmentMessage(
        secretKey: ByteArray,
        prepareIssueMessage: ByteArray
    ): String {
        TODO("Not yet implemented")
    }

    override fun generateHolderSk(): String {
        TODO("Not yet implemented")
    }

    override fun readEuropeanCredential(credential: ByteArray): JSONObject {
        return JSONObject().apply {
            val v0 = JSONObject().apply {
                put("dn", 1)
                put("sd", 1)
                put("co", "NL")
            }
            val v = JSONArray()
            v.put(0, v0)
            val dcc = JSONObject().apply {
                put("v", v)
            }
            put("dcc", dcc)
        }
    }

    override fun initializeHolder(configFilesPath: String): String? {
        TODO("Not yet implemented")
    }

    override fun initializeVerifier(configFilesPath: String): String? {
        TODO("Not yet implemented")
    }

    override fun verify(credential: ByteArray, policy: VerificationPolicy): VerificationResult {
        TODO("Not yet implemented")
    }

    override fun isDcc(credential: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun isForeignDcc(credential: ByteArray): Boolean {
        TODO("Not yet implemented")
    }
}
