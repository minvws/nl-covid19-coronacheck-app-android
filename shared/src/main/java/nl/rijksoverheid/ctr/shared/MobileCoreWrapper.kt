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
import nl.rijksoverheid.ctr.shared.ext.successJsonObject
import nl.rijksoverheid.ctr.shared.ext.successString
import nl.rijksoverheid.ctr.shared.ext.toObject
import nl.rijksoverheid.ctr.shared.ext.verify
import nl.rijksoverheid.ctr.shared.models.DomesticCredential
import nl.rijksoverheid.ctr.shared.models.DomesticCredentialAttributes
import nl.rijksoverheid.ctr.shared.models.ReadDomesticCredential
import org.json.JSONObject
import java.lang.reflect.Type

interface MobileCoreWrapper {
    fun loadIssuerPks(bytes: ByteArray)
    fun createCredentials(body: ByteArray): String
    fun readDomesticCredential(credential: ByteArray): ReadDomesticCredential
    fun readCredential(credentials: ByteArray): ByteArray
    fun readCredentialLegacy(credentials: ByteArray): DomesticCredential
    fun createCommitmentMessage(secretKey: ByteArray, prepareIssueMessage: ByteArray): String
    fun disclose(secretKey: ByteArray, credential: ByteArray): String
    fun generateHolderSk(): String
    fun createDomesticCredentials(createCredentials: ByteArray): List<DomesticCredential>
    fun readEuropeanCredential(credential: ByteArray): JSONObject
    fun initializeVerifier(configFilesPath: String)
    fun verify(credential: ByteArray): Result
}

class MobileCoreWrapperImpl(private val moshi: Moshi) : MobileCoreWrapper {
    override fun loadIssuerPks(bytes: ByteArray) {
        Mobilecore.loadDomesticIssuerPks(bytes)
    }

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

    //DomesticCredential(credential={"signature":{"A":"mJMEPacFYBT+7H0a+501pR9S3i5ADmjm4vKmMb1M469b1ArqXO+LOa+sUREgg46GdXDNDud3ptqiDepP+8r8HrFaIKcH\/2zayWCr5qvngKApCbRiubTGlUmjhVpmz\/U\/jCoSL+84ii4RzdK+g9XJMB38h\/YKOkUNds5vUZ4brcMvD4gd4mMUV85C0yh\/dl4oziGTmMUFhG\/A5xbdp9nfro1f4vbzNR8T6W4a8Fas7D1eYe649Ax\/V8DnWHLoHueoQMhOk5XNnU5Z3h6gox\/Gr9mTrbhItJR8TJ4nfz9d\/HiKgv8UW6brd\/dNnIdPn2NyOjHaUpk\/F5dRF8djhmpyVA==","e":"EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU9hoFh+jd0oKOSgYShkh","v":"CS0oVWy1jgq3UTWJv6WAWJNOC4NyHmMmrf4xPUbbmvpdyjuZ\/30bpKzICKx3y15YCHDnsOPbSyngpNv457BMjKDS3rJxVBODUsYC9SZ57eIpI0q2wF+5yu50Y1wV1rSTvKNNmkbvo0fNj1JwSflNUyFUf9gJjETvNA0gYeKonmcgQExAQRtvUPrvS28udTBL7AbePWQwNJ5nffNO7V9E6NQiUf+Gt0DYcef+JAFPiem8dFZCmdt99+b+gAfbcFUwfWUGzSS6L8MCcVDtnfAw\/BxydDmvWXy8wpeuvm7tqN2yT8a1NnsJR3tLz1+z7rTFcjgMKR2LSQG7w6M7DCE\/U29+aUlGSbowKDc4mWInMMwMv07h9ZmTnCjJ4qH1aYf\/TI+WN+Iuq5ejYpd0t0y2XgnhuN2+2\/f9aWkGm9LY2CrPll9uwIv38NgyD7RltTA6tKVGDxvbJLmdv74cVNORfhs=","KeyshareP":null},"attributes":["s6H7bXA76Fa4g7hDBKq4IDcng3xKCdx\/vWJ1lm7KQDs=","YB4IAgQmFKyuplqoiqaoWmE=","YQ==","YQ==","YmxkZG5mYmxoaw==","ZGk=","hQ==","AQ==","AQ==","bQ=="]}, attributes=DomesticCredentialAttributes(birthDay=, birthMonth=6, credentialVersion=2, firstNameInitial=B, isSpecimen=0, lastNameInitial=, stripType=0, validForHours=24, validFrom=1622731645))
    override fun readCredentialLegacy(credentials: ByteArray): DomesticCredential {
        return DomesticCredential(

            credential = JSONObject().apply {
                put("A", "mJMEPacFYBT+7H0a+501pR9S3i5ADmjm4vKmMb1M469b1ArqXO+LOa+sUREgg46GdXDNDud3ptqiDepP+8r8HrFaIKcH/2zayWCr5qvngKApCbRiubTGlUmjhVpmz/U/jCoSL+84ii4RzdK+g9XJMB38h/YKOkUNds5vUZ4brcMvD4gd4mMUV85C0yh/dl4oziGTmMUFhG/A5xbdp9nfro1f4vbzNR8T6W4a8Fas7D1eYe649Ax/V8DnWHLoHueoQMhOk5XNnU5Z3h6gox/Gr9mTrbhItJR8TJ4nfz9d/HiKgv8UW6brd/dNnIdPn2NyOjHaUpk/F5dRF8djhmpyVA==")
                put("e", "EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU9hoFh+jd0oKOSgYShkh")
                put("v", "CS0oVWy1jgq3UTWJv6WAWJNOC4NyHmMmrf4xPUbbmvpdyjuZ\\/30bpKzICKx3y15YCHDnsOPbSyngpNv457BMjKDS3rJxVBODUsYC9SZ57eIpI0q2wF+5yu50Y1wV1rSTvKNNmkbvo0fNj1JwSflNUyFUf9gJjETvNA0gYeKonmcgQExAQRtvUPrvS28udTBL7AbePWQwNJ5nffNO7V9E6NQiUf+Gt0DYcef+JAFPiem8dFZCmdt99+b+gAfbcFUwfWUGzSS6L8MCcVDtnfAw\\/BxydDmvWXy8wpeuvm7tqN2yT8a1NnsJR3tLz1+z7rTFcjgMKR2LSQG7w6M7DCE\\/U29+aUlGSbowKDc4mWInMMwMv07h9ZmTnCjJ4qH1aYf\\/TI+WN+Iuq5ejYpd0t0y2XgnhuN2+2\\/f9aWkGm9LY2CrPll9uwIv38NgyD7RltTA6tKVGDxvbJLmdv74cVNORfhs=")
                put("KeyshareP", "null")
                put("attributes", "[\"s6H7bXA76Fa4g7hDBKq4IDcng3xKCdx\\/vWJ1lm7KQDs=\",\"YB4IAgQmFKyuplqoiqaoWmE=\",\"YQ==\",\"YQ==\",\"YmxkZG5mYmxoaw==\",\"ZGk=\",\"hQ==\",\"AQ==\",\"AQ==\",\"bQ==\"]")
            },
            attributes = DomesticCredentialAttributes(
                birthDay = "",
                birthMonth = "6",
                credentialVersion = 2,
                firstNameInitial = "B",
                isSpecimen = "0",
                lastNameInitial = "",
                stripType = "0",
                validForHours = 24,
                validFrom = 1622731645L,
            ),
        )
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

    override fun initializeVerifier(configFilesPath: String) = Mobilecore.initializeVerifier(configFilesPath)

    override fun verify(credential: ByteArray): Result = Mobilecore.verify(credential)
}
