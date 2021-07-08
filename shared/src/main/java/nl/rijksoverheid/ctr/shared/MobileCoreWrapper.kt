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
import mobilecore.Mobilecore
import mobilecore.Result
import nl.rijksoverheid.ctr.shared.ext.*
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import org.json.JSONObject
import java.lang.reflect.Type

interface MobileCoreWrapper {
    fun createCredentials(body: ByteArray): String
    fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential
    fun readCredential(credentials: ByteArray): ByteArray
    fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String
    fun disclose(secretKey: ByteArray, credential: ByteArray): String
    fun generateHolderSk(): String
    fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential>
    fun readEuropeanCredential(credential: ByteArray): JSONObject
    // returns error message, if initializing failed
    fun initializeHolder(configFilesPath: String): String?
    // returns error message, if initializing failed
    fun initializeVerifier(configFilesPath: String): String?
    fun verify(credential: ByteArray): Result
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

    override fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String {
        return Mobilecore.createCommitmentMessage(
            secretKey,
            prepareIssueMessage
        ).successString()
    }

    override fun disclose(secretKey: ByteArray, credential: ByteArray): String {
        return Mobilecore.disclose(
            secretKey,
            credential
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

    override fun verify(credential: ByteArray): Result = Mobilecore.verify(credential)
}
