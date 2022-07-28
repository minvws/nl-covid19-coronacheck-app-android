/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type
import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.shared.exceptions.CreateCommitmentMessageException
import nl.rijksoverheid.ctr.shared.ext.successJsonObject
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.models.VerificationResult
import nl.rijksoverheid.ctr.shared.models.VerificationResultDetails
import org.json.JSONObject

interface MobileCoreWrapper {
    fun createCredentials(body: ByteArray): String
    fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential
    fun readCredential(credentials: ByteArray): ByteArray
    fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String
    fun disclose(secretKey: ByteArray, credential: ByteArray, currentTimeMillis: Long, disclosurePolicy: GreenCardDisclosurePolicy): String
    fun generateHolderSk(): String
    fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential>
    fun readEuropeanCredential(credential: ByteArray): JSONObject

    // returns error message, if initializing failed
    fun initializeHolder(configFilesPath: String): String?

    // returns error message, if initializing failed
    fun initializeVerifier(configFilesPath: String): String?
    fun verify(credential: ByteArray, policy: VerificationPolicy): VerificationResult
    fun isDcc(credential: ByteArray): Boolean
    fun isForeignDcc(credential: ByteArray): Boolean
    fun hasDomesticPrefix(credential: ByteArray): Boolean
}

class MobileCoreWrapperImpl(private val moshi: Moshi) : MobileCoreWrapper {

    override fun createCredentials(body: ByteArray): String {
        return Mobilecore.createCredentials(
            body
        ).successString()
    }

    override fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential {
        return Mobilecore.readDomesticCredential(credential).successString().toObject(moshi)
    }

    override fun readCredential(credentials: ByteArray): ByteArray {
        return Mobilecore.readDomesticCredential(credentials).verify()
    }

    @Throws(CreateCommitmentMessageException::class)
    override fun createCommitmentMessage(
        secretKey: ByteArray,
        prepareIssueMessage: ByteArray
    ): String {
        val result = Mobilecore.createCommitmentMessage(
            secretKey,
            prepareIssueMessage
        )
        if (result.error.isNotEmpty()) {
            throw CreateCommitmentMessageException()
        }
        return String(result.value)
    }

    override fun disclose(secretKey: ByteArray, credential: ByteArray, currentTimeMillis: Long, disclosurePolicy: GreenCardDisclosurePolicy): String {
        val disclosurePolicyString = when (disclosurePolicy) {
            is GreenCardDisclosurePolicy.OneG -> {
                Mobilecore.DISCLOSURE_POLICY_1G
            }
            is GreenCardDisclosurePolicy.ThreeG -> {
                Mobilecore.DISCLOSURE_POLICY_3G
            }
        }

        return Mobilecore.discloseWithTime(
            secretKey,
            credential,
            disclosurePolicyString,
            currentTimeMillis / 1000L
        ).successString()
    }

    override fun generateHolderSk(): String {
        return Mobilecore.generateHolderSk().successString()
    }

    override fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential> {
        val createCredentialsResult =
            Mobilecore.createCredentials(createCredentials).successString()

        val type: Type = Types.newParameterizedType(
            List::class.java,
            DomesticCredential::class.java
        )

        val adapter: JsonAdapter<List<DomesticCredential>> = moshi.adapter(type)
        return adapter.fromJson(createCredentialsResult)
            ?: throw IllegalStateException("Could not create domestic credentials")
    }

    override fun readEuropeanCredential(credential: ByteArray): JSONObject {
        return Mobilecore.readEuropeanCredential(credential).successJsonObject()
    }

    override fun initializeHolder(configFilesPath: String): String? {
        return try {
            val initResult = Mobilecore.initializeHolder(configFilesPath)
            initResult.error.takeIf { it.isNotEmpty() }
        } catch (exception: Exception) {
            exception.message ?: "unknown initializeHolder library error"
        }
    }

    override fun initializeVerifier(configFilesPath: String): String? {
        return try {
            val initResult = Mobilecore.initializeVerifier(configFilesPath)
            initResult.error.takeIf { it.isNotEmpty() }
        } catch (exception: Exception) {
            exception.message ?: "unknown initializeVerifier library error"
        }
    }

    override fun verify(credential: ByteArray, policy: VerificationPolicy): VerificationResult {
        val result = Mobilecore.verify(credential, policy.libraryValue)
        return VerificationResult(
            status = result.status,
            details = VerificationResultDetails(
                birthDay = result.details?.birthDay ?: "",
                birthMonth = result.details?.birthMonth ?: "",
                firstNameInitial = result.details?.firstNameInitial ?: "",
                lastNameInitial = result.details?.lastNameInitial ?: "",
                isSpecimen = result.details?.isSpecimen ?: "",
                credentialVersion = result.details?.credentialVersion ?: "",
                issuerCountryCode = result.details?.issuerCountryCode ?: ""
            ),
            error = result.error
        )
    }

    override fun isDcc(credential: ByteArray): Boolean {
        return Mobilecore.isDCC(credential)
    }

    override fun isForeignDcc(credential: ByteArray): Boolean {
        return Mobilecore.isForeignDCC(credential)
    }

    override fun hasDomesticPrefix(credential: ByteArray): Boolean {
        return Mobilecore.hasDomesticPrefix(credential)
    }
}
