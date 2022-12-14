package nl.rijksoverheid.ctr.holder.api

import android.util.Base64
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.slot
import io.mockk.spyk
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import nl.rijksoverheid.ctr.api.signing.certificates.PRIVATE_ROOT_CA
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit

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
        val certificateBytes = listOf(Base64.encode(PRIVATE_ROOT_CA.toByteArray(), Base64.DEFAULT))

        HolderApiClientUtilImpl(okHttpClient, retrofit).client(certificateBytes)

        val trustManager = trustManagerSlot.captured
        assertEquals(certificateBytes.size, trustManager.acceptedIssuers.size)
        assertTrue(trustManager.acceptedIssuers.first().subjectDN.name.contains("Staat der Nederlanden Private Root CA - G1"))
    }

    @Test
    fun `every test api client uses only the certificates passed`() {
        val tlsCertificateBytes = listOf(
            Base64.decode(
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUlPVENDQmlHZ0F3SUJBZ0lVZW5nWlRpUmw4am8wZE9nV3djZ1ZmL3p0NEhzd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1NURUxNQWtHQTFVRUJoTUNUa3d4RVRBUEJnTlZCQW9NQ0V0UVRpQkNMbFl1TVNjd0pRWURWUVFEREI1TApVRTRnVUV0SmIzWmxjbWhsYVdRZ1UyVnlkbVZ5SUVOQklESXdNakF3SGhjTk1qRXhNakl5TURrMU1EQXlXaGNOCk1qSXdPVEU0TURrMU1EQXlXakNCZ3pFTE1Ba0dBMVVFQmhNQ1Rrd3hGakFVQmdOVkJBY01EU2R6TFVkeVlYWmwKYm1oaFoyVXhPVEEzQmdOVkJBb01NRTFwYm1semRHVnlhV1VnZG1GdUlGWnZiR3R6WjJWNmIyNWthR1ZwWkN3ZwpWMlZzZW1scWJpQmxiaUJUY0c5eWRERWhNQjhHQTFVRUF3d1lZWEJwTFhSbGMzUXVZMjl5YjI1aGRHVnpkR1Z5CkxtNXNNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQTZadlJOSGlQMXFUbWVnN0gKc3BiRVlqU0lncDdQNXlCUHFrN1N5UnFsdXhoS3F4amVmT2NmRUZoYklEUHJLNU9FZXpLYUFpdktBaHpBN1E2MwplaTk2S1N2RE9WRUs5VjVIZzFDb0xwVXBJdnkrdjljYWtYS1BBLzVnYS9mZzBWWGtPTFl1Q2g1ZndpYkdJOTZlCk5LVFBvWU5XZG53SHdtWGVVOEhreHRrbGRRNDBxMGU2bHRacU1GZm5EdWs5WEpLeU1xS1ZKSER0T1ZyaWIwYm8KSnBRaXFuT0pONW00a1lyUjVuMnIyd0ZIWGtVUGhyMkFkN0dIMStBOHRCQ1RXSm1VeFg5VW44SEJqNDRGbnEzNQpUYWZ0dVErbWVkS3VTblJkVGZxem1acHhTRm1FRzFTbXAwbHYxQnpCcUJkNE53eXk4VFpITHdrc2s1ZmY3VDZyCi80cFhYUUlEQVFBQm80SUQzRENDQTlnd0RBWURWUjBUQVFIL0JBSXdBREFmQmdOVkhTTUVHREFXZ0JRSVNxcTcKbVNSdnZsc0g4YVdLbVZzdFIrKzVQRENCaVFZSUt3WUJCUVVIQVFFRWZUQjdNRTBHQ0NzR0FRVUZCekFDaGtGbwpkSFJ3T2k4IHZZMlZ5ZEM1dFlXNWhaMlZrY0d0cExtTnZiUzlEUVdObGNuUnpMMHRRVGxCTFNXOTJaWEpvWldsawpVMlZ5ZG1WeVEwRXlNREl3TG1ObGNqQXFCZ2dyQmdFRkJRY3dBWVllYUhSMGNEb3ZMMjlqYzNBeU1ESXdMbTFoCmJtRm5aV1J3YTJrdVkyOXRNQ01HQTFVZEVRUWNNQnFDR0dGd2FTMTBaWE4wTG1OdmNtOXVZWFJsYzNSbGNpNXUKYkRCY0JnTlZIU0FFVlRCVE1BZ0dCbWVCREFFQ0FqQkhCZ3BnaEJBQmgyc0JBZ1VKTURrd053WUlLd1lCQlFVSApBZ0VXSzJoMGRIQnpPaTh2WTJWeWRHbG1hV05oWVhRdWEzQnVMbU52YlM5d2EybHZkbVZ5YUdWcFpDOWpjSE13CkhRWURWUjBsQkJZd0ZBWUlLd1lCQlFVSEF3SUdDQ3NHQVFVRkJ3TUJNRk1HQTFVZEh3Uk1NRW93U0tCR29FU0cKUW1oMGRIQTZMeTlqY213dWJXRnVZV2RsWkhCcmFTNWpiMjB2UzFCT1VFdEpiM1psY21obGFXUlRaWEoyWlhKRApRVEl3TWpBdlRHRjBaWE4wUTFKTUxtTnliREFkQmdOVkhRNEVGZ1FVbEZTWFQvcGYwc2lGT0crU2tBVEtmbUZOCkRSY3dEZ1lEVlIwUEFRSC9CQVFEQWdXZ01JSUI4d1lLS3dZQkJBSFdlUUlFQWdTQ0FlTUVnZ0hmQWQwQWRRQkcKcFZYcmRmcVJJREMxb29scDlQTjlFU3hCZEw3OVNiaUZxL0w4Y1A1dFJ3QUFBWDNoakVEVEFBQUVBd0JHTUVRQwpJQ1J1cGx3SGN4R3JpR1BrQlkycXh1aTdUVnkxOStyUmRzQ3VmczJKYUlzdkFpQi9RS3lCS0xCczB2MTJmZ2xSCnBTNlJRRytLKzd2cURUZXp3MFc5cHhjTFlRQjFBRUhJeXJIZklrWktFTWFoT2dsQ2gxNU9NWXNiQSt2clM4ZG8KOEpCaWxnYjJBQUFCZmVHTVFUUUFBQVFEQUVZd1JBSWdIc0RUdEUvc2pLWnNiVG9YTExhRUQ5ZmxsOHZLZUZsOQowTVA1Y3MrcksvWUNJR05DdHd2c2hlSCtPdUlXTnJ4a2tkbTkxdXFZMlFHYjJaNFVmZ3FwOW9iMEFIWUFWWUhVCndoYVFOZ0ZLNmd1YlZ6eFQ4TURrT0hod0pRZ1hMNk9xSFFjVDB3d0FBQUY5IDRZeENIUUFBQkFNQVJ6QkZBaUFDCnF5dzhyaGhnSE1GUlhWMDZsTjJVbytZQk1SenFuYlBTTENuVDJ6OXVSZ0loQUlTN1FjOWh2d0s3WU94bjZBcTIKa1ROOW44WWJLOEpGUDBxeFF5NllCWUhXQUhVQWIxTjJyREh3TVJuWW1RQ2tVUlgvZHhVY0Vka0N3UUFwQm8yeQpDSm8zMlJNQUFBRjk0WXhCRlFBQUJBTUFSakJFQWlBUXJYRzJJbEZHa1VpM3JlVEhuUHorUHhIRHNDcklsdTZaCmVuVE5mR1VCS0FJZ1FqZDZtL1BJL2F0c2YvVlgvbGRHWm5QUWE1cklMVnlZNWRtNGYveWpZb0F3RFFZSktvWkkKaHZjTkFRRUxCUUFEZ2dJQkFNWXdnekVabndjSjNzNmVpTVJtc1hhUExsV1RIaUJmUmxTTjNmdE9tQWFCaXVBVQpKZmZMeFpPSWM1WUtYalB2WkNKS1ZGR1h6ajhqeVdnQ1d2eE5jSnlrUEtXTjRaMTQrdjlSeHVBUW40V2UrOUFICml1TFp0cFpXY0h5OWNOMkU2WTM5d25MVkZ0dHg2cFVhS3ZwaS9FYmdGK2FzMklpbTB6elV2YTgvL2cvR1psb2oKMlpnZE9qdFFXdHhSNytTTnRaTWVyUUl4NnUwVHd2eVRRNncxUVg0OEJrK3FaV05QU1Q3T0syQW1aSUhDcWxkVQpUVElQVEpDVEV3UzNUdHV6dnFqbWRyMU1oMnhtTWFUNUMxcTlZamNrbjhnTm5aKytWN3NmNEpRY1kyVWRrQ204CmhDS0ZuZ2N3a1UwQUorTjI2WVBHVTBuMVJ0MDBmQXBkOCtzaEZtdU1obWFVUnhCRHRWMGNHVE1aK2pRTWFqT0IKRVFvVTRpbXdtMjkrTzRIckdCcGYzc1VDenlVa2pDYVZJT0JnNUk1NTllVmNqZk9XcDZnZzZjc3NuaDJ6ajRhMwpmOGdtdEJVZjJLV1A3NkhqUTc0ZnNKNlN4VU51WERDWVJNNHNna3V6QVNRSkpTbzVNU1dWRDRHTEw3bVlyb01TCm9QNkNEYk54NHN1bE1neTJ4Z0ZxK3JhaHBDN1A4VEZqWjFxWm5rZm1HYWtKbzZrNEVhcVdadFNtRjhJT25meUQKKzl5UGMxQ0pyZ09tbW5pZU5uRFR5WTBMKzRBbjdiOWE4ekpmblZKRnlqalRmUU0rcmFUTzBGRGU2cXZxWFdpdgp2R0wxTU5QaC9WSjB6IEpjM201WlpsSGVNZUEzN0wxcCtJbnE1VHhFK0wxNmNvd0N1UjBnd2xOSktnbkROCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K".toByteArray(),
                Base64.DEFAULT
            )
        )
        val cmsCertificateBytes = listOf(
            Base64.decode(
                "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZoRENDQTJ5Z0F3SUJBZ0lFQUppbUlUQU5CZ2txaGtpRzl3MEJBUXNGQURCaU1Rc3dDUVlEVlFRR0V3Sk8KVERFZU1Cd0dBMVVFQ2d3VlUzUmhZWFFnWkdWeUlFNWxaR1Z5YkdGdVpHVnVNVE13TVFZRFZRUUREQ3BUZEdGaApkQ0JrWlhJZ1RtVmtaWEpzWVc1a1pXNGdVSEpwZG1GMFpTQlNiMjkwSUVOQklDMGdSekV3SGhjTk1UTXhNVEUwCk1UTTBPRFUxV2hjTk1qZ3hNVEV6TWpNd01EQXdXakJpTVFzd0NRWURWUVFHRXdKT1RERWVNQndHQTFVRUNnd1YKVTNSaFlYUWdaR1Z5SUU1bFpHVnliR0Z1WkdWdU1UTXdNUVlEVlFRRERDcFRkR0ZoZENCa1pYSWdUbVZrWlhKcwpZVzVrWlc0Z1VISnBkbUYwWlNCU2IyOTBJRU5CSUMwZ1J6RXdnZ0lpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElDCkR3QXdnZ0lLQW9JQ0FRRGFJTWg1Nnlud25FaEU3RXk1NEtwWDVqMVhEb3hiSERDZ1hjdHV0ZTU1UmptRzJoeTYKZnVxKytxL2RDU3NqMzhQaS9LWW4vUE4xM0VGMDVrMzlJUnZha2IwQVFOVnlIaWZOS1hmdGE2VHppNVFjTTRCSwowOURCNENrYjZUZFpUTlV0V3lFY0F0UmJsWWFWU1E0WHI1UU9ETnF1MkZHUXVjcmFWWHFDSXg4MWF6bE9FMkpiClpsaTlBWktuOTRwUDU3QTExZFVZaHhNc2g3MFlvc0pFS1ZCOFVlNFJPa3NIaGIvbm5PSVNHKzJ5OUZENU04dTgKallocDAwVEdaR1Z1NXowSUZndHFYMGk4R21ySDB1YjlBV2pmL2lVNE1XakdWUlNxMGN3VUhFZUtSai9VRDlhOAp4SUVuOVR4SWZZais2K3M0dG45ZFcvNFBWNWpjNmlHSng2RXhUUGZPUjdWSHB4UzRYdWpyWmI1QmEvK29qL09OCmRPZlIwSlNtMml0Q3l0YnRqUUJCTDBvb2NJSXFhcU9uYTFjdWZIa2NuOVZsZUY3WnZ6LzhualFJcEFVNEo0bkoKNHBFNXBRM2s0T1JBR05ucTVSOWhBcXFVUUdEbG8zVWo4UEJvdTBuUHpRN0pOZ0drTitteS9sR3I0cmNlVU5LLwo4Q29HbllGVUgrVXlGdEprdmxMbEVrYjY4OC9JZE5kR2dZK3Z1WENBQjZ4ZktsSmpBR0NoRlVCYjZzd2JOZU5jCnRWRWRVajdXZWc0SnQ1Z1h1NzhDMm1qczl4NWxjSE9nTU80Wm12WUozRWpwNGszbk5hNDVIT0lWa1lyZlFyckIKSHpCaFIwQnVSZUFhZ3VyY2J0VWpKRmQ3QnR1ZkdWTGZVM0NVbjFsNnUzLzllRzRER0g2cHErZFNLUUlEQVFBQgpvMEl3UURBUEJnTlZIUk1CQWY4RUJUQURBUUgvTUE0R0ExVWREd0VCL3dRRUF3SUJCakFkQmdOVkhRNEVGZ1FVCkt2MjVLeDc2dzRTSEJ0dUIvNGFYZFEzckFZc3dEUVlKS29aSWh2Y05BUUVMQlFBRGdnSUJBRXZwbVhNT09LZFEKd1VQeXNyc2RJa0dKVUZGK2R2bXNKRGlPdUFxVjBBMW5OVG9vTDNlc3ZETEVaQVdad0tUT3dSb21uSHplQ2ZTLwpReFJLVGtWWDIxcGZySGY5dWZES3lrcHpqbDl1QUlMVFM3NkZKNi8vUjBSVElQTXJ6a25RcEcyZkNMUjVERkViCkhXVS9qV0F4R21uY2Z4NkhRWWwvYXpIYVdidjBkaFpPVWpQZGtHQVE2RVB2SGN5TlU5eU1rRVRkdzBYNmlveHEKek13a0dNODkzb0JyTW10ZHVpcUlmMy9INkhUWG9SS0FjKy9EWFpJcS9wQWM2ZVZNYTZ4NDNrb2tsdWFhbTlMNwo4eURybEhiR2QyVllBci9IWjBUakRaVHRJMnQyL3lTVGI3SmpDOHdMOHJTcXhZbUxwTnJuaFp6UFc4N3NsMk9DCkZDM3JlM1podEprSUhOUDg1amoxZ3Fld1RDN0RDVzZsbFpkQjNoQnpmSFdieTBFWDJSbGN3Z2FNZk5CRVY1VTAKSW9nY2NkWFYrUzZ6V0s0Rit5QnIwc1hVcmRiZE1GdStnM0k5Q2JYeHQwcTRlVkp0b2F1bjRNMlorYlpNcVp2eQo5RnJ5QmRTZmhwZ21KcXdGejJsdU9oUE9WQ2JsQ1BoTHJVZWV3cnZ1QlhvWlFXdDFaanVIZndKWjFkZ2pzelZFCnF3WTlTMFNkcUNnMlpsTDlzM3ZESXJyZDN3TFdyY0hMUU1kOWd3c3BwTnY5YzdKZklKZGxjWkxUbUY5RXVMNmUKQ3ZWVnJxQlZxTEhqdmE0ZXJxWW9sNksvamJTZlV0UkN5OElsRlU3TFl1MUtMZWhaS1l2ajN2ZWtqM0NuMDhBcQpsanIvUThQdytPZlVaVHpLZzRQVkRRVmZGcUt0eW9zdgotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==",
                Base64.DEFAULT
            )
        )

        TestProviderApiClientUtilImpl(Moshi.Builder().build(), okHttpClient, retrofit).client(
            tlsCertificateBytes, cmsCertificateBytes
        )

        val trustManager = trustManagerSlot.captured
        assertEquals(2, trustManager.acceptedIssuers.size)
        assertTrue(trustManager.acceptedIssuers.first().subjectDN.name.contains("api-test.coronatester.nl"))
        assertTrue(trustManager.acceptedIssuers[1].subjectDN.name.contains("Staat der Nederlanden Private Root CA - G1"))
    }
}
