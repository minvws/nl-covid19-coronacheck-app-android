package nl.rijksoverheid.ctr.holder.data_migration

import com.squareup.moshi.Moshi
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.api.json.JsonObjectJsonAdapter
import nl.rijksoverheid.ctr.api.json.LocalDateJsonAdapter
import nl.rijksoverheid.ctr.api.json.OffsetDateTimeJsonAdapter
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataMigrationImportUseCaseImplTest : AutoCloseKoinTest() {

    private val moshi = Moshi.Builder()
        .add(Base64JsonAdapter())
        .add(JsonObjectJsonAdapter())
        .add(OffsetDateTimeJsonAdapter())
        .add(LocalDateJsonAdapter())
        .build()

    private val stringDataZipper = mockk<StringDataZipper>()

    private val dataMigrationUseCase = DataMigrationImportUseCaseImpl(moshi, stringDataZipper)

    @Test
    fun `convert migration qr string to migration parcel`() = runTest {
        val content =
            "eyJpIjo4LCJuIjoxNywicCI6InpGSVUzams4K0xXeWNkYVBJK1NwanpMMkpjdnhJMnRUQlB5V1ZLaiswTzdZUU8wSkhrTnlGS3d2Qm55QWRyckVlWGpyQ1lIcXRrSFJUVEZrMXcySDRXSk9oQ0FMdnZEWVFJaURWUU0xSmdhTitHNDZYRUtjS0lVYWdvSkVpay9JTExUVExxVGRrQzFuSlZEbE5TNGZ3UDlDTzZxWWJFQXpWaDRQMHY0M3FOZWt1WFFVZU5LMHdGSEFrL1lFV1FxWURNc3NXczliWkhicGtwZ0xTVUdmd3hyMEhYZzRoajRIcWR1ODYzamJBZXQ3aFRmZVRmZDQvdFFQREcwMVRoMGZGclYrVnMxbklRMHpDdGFQcHpPMldFY0swT09ZVGFDM3BleDgwRnUvaEl6OEJGNWM5d3VjYjcyUjYyZlFHMkNVVmU4ZzV3OUNHbWdiY1lpUXZoNXdiQ0RCbE5KOEZhTHpURDlpcGFKN0dVSWN0U3dRdis4bmQralhmaktOVWdIdEYyLzM3L01pZ1hibjMvTklia0JvMGE0UU9HYWhkaUdqZjJUeGRyeEVpYjNnWklGNVRvc2Vuem1pRDRMcG5Zdmk4WDMrcFlBN1paSnpvWmx4R0lER3BETUpIQ3B0VjNmcmVhVGRzUWhUME9NSGFBQjRUWElqQTRYMm1JRjFLSUo2RFN0Qk1STjRuOFhyZnZrNkZ4WVlrd1dRd1FmdEpldStiYnpxTlBCdHBxQjJvUDRlYTM2aDNleWZnYWVCcS8rZDhUWWd6NnMvSHFBV0U2eHlrN0VvQ1N2SVRCb0ZkOFdiWC9kalArZHZzTUR6MGl0NDhnbDVaSUx4YjZCT0htcmxmNVdFa0YydnBNVi93L3QvczkvVG9lODhVeXcwWThEYnhtT3dmaVJTbkI1cjRIZjZhN3paSHNiL3V2WEU5N2tBckxmK0g1LzNBZlArT3UrRDM1ZVJjM1RuR1hJVmtXanN1SXRURTBtWS8rQVZXUFJGdThrTGVOUk5Qbis3S3pFK1pJKy93RWRUNExscnhadVVEL1VIV1hSd1Z2T1F0czB0IiwidiI6IkNDMSJ9"

        val migrationParcel = dataMigrationUseCase.import(content)

        assertNotNull(migrationParcel)
        assertEquals("CC1", migrationParcel.version)
        assertEquals(8, migrationParcel.index)
        assertEquals(17, migrationParcel.numberOfPackages)
    }
}
