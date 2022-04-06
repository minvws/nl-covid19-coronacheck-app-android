package nl.rijksoverheid.ctr.holder.api

import android.util.Base64
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import nl.rijksoverheid.ctr.api.signing.certificates.EMAX_ROOT_CA
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import kotlin.test.assertEquals


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class ApiClientUtilTests : AutoCloseKoinTest() {
    private val mockWebServer = MockWebServer()
    private val builder = spyk(OkHttpClient.Builder())
    private val okHttpClient = spyk(builder.build())
    private val retrofit = Retrofit.Builder().baseUrl(mockWebServer.url("/")).build()
    private val sslSocketFactory = slot<SSLSocketFactory>()
    private val trustManagerSlot = slot<X509TrustManager>()

    @Before
    fun setup() {
        every { okHttpClient.newBuilder() } returns builder
        every {
            builder.sslSocketFactory(
                capture(sslSocketFactory),
                capture(trustManagerSlot)
            )
        } returns builder
        every { builder.build() } returns okHttpClient
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `every holder api client uses only the certificates passed`() {
        val certificateBytes = listOf(Base64.encode(EMAX_ROOT_CA.toByteArray(), Base64.DEFAULT))

        HolderApiClientUtilImpl(okHttpClient, retrofit).client(certificateBytes)

        val trustManager = trustManagerSlot.captured
        assertEquals(certificateBytes.size, trustManager.acceptedIssuers.size)
        assertEquals(
            "CN=emax.acc.coronacheck.nl",
            trustManager.acceptedIssuers.first().subjectDN.name
        )
    }

    @Test
    fun `every test api client uses only the certificates passed`() {
        val certificateBytes = listOf(
            Base64.decode(
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUlPVENDQmlHZ0F3SUJBZ0lVZW5nWlRpUmw4am8wZE9nV3djZ1ZmL3p0NEhzd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1NURUxNQWtHQTFVRUJoTUNUa3d4RVRBUEJnTlZCQW9NQ0V0UVRpQkNMbFl1TVNjd0pRWURWUVFEREI1TApVRTRnVUV0SmIzWmxjbWhsYVdRZ1UyVnlkbVZ5SUVOQklESXdNakF3SGhjTk1qRXhNakl5TURrMU1EQXlXaGNOCk1qSXdPVEU0TURrMU1EQXlXakNCZ3pFTE1Ba0dBMVVFQmhNQ1Rrd3hGakFVQmdOVkJBY01EU2R6TFVkeVlYWmwKYm1oaFoyVXhPVEEzQmdOVkJBb01NRTFwYm1semRHVnlhV1VnZG1GdUlGWnZiR3R6WjJWNmIyNWthR1ZwWkN3ZwpWMlZzZW1scWJpQmxiaUJUY0c5eWRERWhNQjhHQTFVRUF3d1lZWEJwTFhSbGMzUXVZMjl5YjI1aGRHVnpkR1Z5CkxtNXNNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQTZadlJOSGlQMXFUbWVnN0gKc3BiRVlqU0lncDdQNXlCUHFrN1N5UnFsdXhoS3F4amVmT2NmRUZoYklEUHJLNU9FZXpLYUFpdktBaHpBN1E2MwplaTk2S1N2RE9WRUs5VjVIZzFDb0xwVXBJdnkrdjljYWtYS1BBLzVnYS9mZzBWWGtPTFl1Q2g1ZndpYkdJOTZlCk5LVFBvWU5XZG53SHdtWGVVOEhreHRrbGRRNDBxMGU2bHRacU1GZm5EdWs5WEpLeU1xS1ZKSER0T1ZyaWIwYm8KSnBRaXFuT0pONW00a1lyUjVuMnIyd0ZIWGtVUGhyMkFkN0dIMStBOHRCQ1RXSm1VeFg5VW44SEJqNDRGbnEzNQpUYWZ0dVErbWVkS3VTblJkVGZxem1acHhTRm1FRzFTbXAwbHYxQnpCcUJkNE53eXk4VFpITHdrc2s1ZmY3VDZyCi80cFhYUUlEQVFBQm80SUQzRENDQTlnd0RBWURWUjBUQVFIL0JBSXdBREFmQmdOVkhTTUVHREFXZ0JRSVNxcTcKbVNSdnZsc0g4YVdLbVZzdFIrKzVQRENCaVFZSUt3WUJCUVVIQVFFRWZUQjdNRTBHQ0NzR0FRVUZCekFDaGtGbwpkSFJ3T2k4IHZZMlZ5ZEM1dFlXNWhaMlZrY0d0cExtTnZiUzlEUVdObGNuUnpMMHRRVGxCTFNXOTJaWEpvWldsawpVMlZ5ZG1WeVEwRXlNREl3TG1ObGNqQXFCZ2dyQmdFRkJRY3dBWVllYUhSMGNEb3ZMMjlqYzNBeU1ESXdMbTFoCmJtRm5aV1J3YTJrdVkyOXRNQ01HQTFVZEVRUWNNQnFDR0dGd2FTMTBaWE4wTG1OdmNtOXVZWFJsYzNSbGNpNXUKYkRCY0JnTlZIU0FFVlRCVE1BZ0dCbWVCREFFQ0FqQkhCZ3BnaEJBQmgyc0JBZ1VKTURrd053WUlLd1lCQlFVSApBZ0VXSzJoMGRIQnpPaTh2WTJWeWRHbG1hV05oWVhRdWEzQnVMbU52YlM5d2EybHZkbVZ5YUdWcFpDOWpjSE13CkhRWURWUjBsQkJZd0ZBWUlLd1lCQlFVSEF3SUdDQ3NHQVFVRkJ3TUJNRk1HQTFVZEh3Uk1NRW93U0tCR29FU0cKUW1oMGRIQTZMeTlqY213dWJXRnVZV2RsWkhCcmFTNWpiMjB2UzFCT1VFdEpiM1psY21obGFXUlRaWEoyWlhKRApRVEl3TWpBdlRHRjBaWE4wUTFKTUxtTnliREFkQmdOVkhRNEVGZ1FVbEZTWFQvcGYwc2lGT0crU2tBVEtmbUZOCkRSY3dEZ1lEVlIwUEFRSC9CQVFEQWdXZ01JSUI4d1lLS3dZQkJBSFdlUUlFQWdTQ0FlTUVnZ0hmQWQwQWRRQkcKcFZYcmRmcVJJREMxb29scDlQTjlFU3hCZEw3OVNiaUZxL0w4Y1A1dFJ3QUFBWDNoakVEVEFBQUVBd0JHTUVRQwpJQ1J1cGx3SGN4R3JpR1BrQlkycXh1aTdUVnkxOStyUmRzQ3VmczJKYUlzdkFpQi9RS3lCS0xCczB2MTJmZ2xSCnBTNlJRRytLKzd2cURUZXp3MFc5cHhjTFlRQjFBRUhJeXJIZklrWktFTWFoT2dsQ2gxNU9NWXNiQSt2clM4ZG8KOEpCaWxnYjJBQUFCZmVHTVFUUUFBQVFEQUVZd1JBSWdIc0RUdEUvc2pLWnNiVG9YTExhRUQ5ZmxsOHZLZUZsOQowTVA1Y3MrcksvWUNJR05DdHd2c2hlSCtPdUlXTnJ4a2tkbTkxdXFZMlFHYjJaNFVmZ3FwOW9iMEFIWUFWWUhVCndoYVFOZ0ZLNmd1YlZ6eFQ4TURrT0hod0pRZ1hMNk9xSFFjVDB3d0FBQUY5IDRZeENIUUFBQkFNQVJ6QkZBaUFDCnF5dzhyaGhnSE1GUlhWMDZsTjJVbytZQk1SenFuYlBTTENuVDJ6OXVSZ0loQUlTN1FjOWh2d0s3WU94bjZBcTIKa1ROOW44WWJLOEpGUDBxeFF5NllCWUhXQUhVQWIxTjJyREh3TVJuWW1RQ2tVUlgvZHhVY0Vka0N3UUFwQm8yeQpDSm8zMlJNQUFBRjk0WXhCRlFBQUJBTUFSakJFQWlBUXJYRzJJbEZHa1VpM3JlVEhuUHorUHhIRHNDcklsdTZaCmVuVE5mR1VCS0FJZ1FqZDZtL1BJL2F0c2YvVlgvbGRHWm5QUWE1cklMVnlZNWRtNGYveWpZb0F3RFFZSktvWkkKaHZjTkFRRUxCUUFEZ2dJQkFNWXdnekVabndjSjNzNmVpTVJtc1hhUExsV1RIaUJmUmxTTjNmdE9tQWFCaXVBVQpKZmZMeFpPSWM1WUtYalB2WkNKS1ZGR1h6ajhqeVdnQ1d2eE5jSnlrUEtXTjRaMTQrdjlSeHVBUW40V2UrOUFICml1TFp0cFpXY0h5OWNOMkU2WTM5d25MVkZ0dHg2cFVhS3ZwaS9FYmdGK2FzMklpbTB6elV2YTgvL2cvR1psb2oKMlpnZE9qdFFXdHhSNytTTnRaTWVyUUl4NnUwVHd2eVRRNncxUVg0OEJrK3FaV05QU1Q3T0syQW1aSUhDcWxkVQpUVElQVEpDVEV3UzNUdHV6dnFqbWRyMU1oMnhtTWFUNUMxcTlZamNrbjhnTm5aKytWN3NmNEpRY1kyVWRrQ204CmhDS0ZuZ2N3a1UwQUorTjI2WVBHVTBuMVJ0MDBmQXBkOCtzaEZtdU1obWFVUnhCRHRWMGNHVE1aK2pRTWFqT0IKRVFvVTRpbXdtMjkrTzRIckdCcGYzc1VDenlVa2pDYVZJT0JnNUk1NTllVmNqZk9XcDZnZzZjc3NuaDJ6ajRhMwpmOGdtdEJVZjJLV1A3NkhqUTc0ZnNKNlN4VU51WERDWVJNNHNna3V6QVNRSkpTbzVNU1dWRDRHTEw3bVlyb01TCm9QNkNEYk54NHN1bE1neTJ4Z0ZxK3JhaHBDN1A4VEZqWjFxWm5rZm1HYWtKbzZrNEVhcVdadFNtRjhJT25meUQKKzl5UGMxQ0pyZ09tbW5pZU5uRFR5WTBMKzRBbjdiOWE4ekpmblZKRnlqalRmUU0rcmFUTzBGRGU2cXZxWFdpdgp2R0wxTU5QaC9WSjB6IEpjM201WlpsSGVNZUEzN0wxcCtJbnE1VHhFK0wxNmNvd0N1UjBnd2xOSktnbkROCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K".toByteArray(),
                Base64.DEFAULT
            )
        )

        TestProviderApiClientUtilImpl(Moshi.Builder().build(), okHttpClient, retrofit).client(
            certificateBytes, certificateBytes
        )

        val trustManager = trustManagerSlot.captured
        assertEquals(certificateBytes.size, trustManager.acceptedIssuers.size)
        assertEquals(
            "CN=api-test.coronatester.nl, O=\"Ministerie van Volksgezondheid, Welzijn en Sport\", L='s-Gravenhage, C=NL",
            trustManager.acceptedIssuers.first().subjectDN.name
        )
    }
}