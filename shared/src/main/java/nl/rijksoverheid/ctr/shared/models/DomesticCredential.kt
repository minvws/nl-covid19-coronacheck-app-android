package nl.rijksoverheid.ctr.shared.models

import org.json.JSONObject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class DomesticCredential(
    val credential: JSONObject,
    val attributes: DomesticCredentialAttributes
)

fun getFakeDomesticCredentials() = DomesticCredential(

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
        validFrom = 1622731645,
    ),
)
//DomesticCredential(credential={"signature":{"A":"mJMEPacFYBT+7H0a+501pR9S3i5ADmjm4vKmMb1M469b1ArqXO+LOa+sUREgg46GdXDNDud3ptqiDepP+8r8HrFaIKcH\/2zayWCr5qvngKApCbRiubTGlUmjhVpmz\/U\/jCoSL+84ii4RzdK+g9XJMB38h\/YKOkUNds5vUZ4brcMvD4gd4mMUV85C0yh\/dl4oziGTmMUFhG\/A5xbdp9nfro1f4vbzNR8T6W4a8Fas7D1eYe649Ax\/V8DnWHLoHueoQMhOk5XNnU5Z3h6gox\/Gr9mTrbhItJR8TJ4nfz9d\/HiKgv8UW6brd\/dNnIdPn2NyOjHaUpk\/F5dRF8djhmpyVA==","e":"EAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAU9hoFh+jd0oKOSgYShkh","v":"CS0oVWy1jgq3UTWJv6WAWJNOC4NyHmMmrf4xPUbbmvpdyjuZ\/30bpKzICKx3y15YCHDnsOPbSyngpNv457BMjKDS3rJxVBODUsYC9SZ57eIpI0q2wF+5yu50Y1wV1rSTvKNNmkbvo0fNj1JwSflNUyFUf9gJjETvNA0gYeKonmcgQExAQRtvUPrvS28udTBL7AbePWQwNJ5nffNO7V9E6NQiUf+Gt0DYcef+JAFPiem8dFZCmdt99+b+gAfbcFUwfWUGzSS6L8MCcVDtnfAw\/BxydDmvWXy8wpeuvm7tqN2yT8a1NnsJR3tLz1+z7rTFcjgMKR2LSQG7w6M7DCE\/U29+aUlGSbowKDc4mWInMMwMv07h9ZmTnCjJ4qH1aYf\/TI+WN+Iuq5ejYpd0t0y2XgnhuN2+2\/f9aWkGm9LY2CrPll9uwIv38NgyD7RltTA6tKVGDxvbJLmdv74cVNORfhs=","KeyshareP":null},"attributes":["s6H7bXA76Fa4g7hDBKq4IDcng3xKCdx\/vWJ1lm7KQDs=","YB4IAgQmFKyuplqoiqaoWmE=","YQ==","YQ==","YmxkZG5mYmxoaw==","ZGk=","hQ==","AQ==","AQ==","bQ=="]}, attributes=DomesticCredentialAttributes(birthDay=, birthMonth=6, credentialVersion=2, firstNameInitial=B, isSpecimen=0, lastNameInitial=, stripType=0, validForHours=24, validFrom=1622731645))