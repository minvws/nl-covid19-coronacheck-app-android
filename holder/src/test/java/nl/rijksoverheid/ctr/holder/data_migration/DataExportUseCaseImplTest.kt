package nl.rijksoverheid.ctr.holder.data_migration

import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DataExportUseCaseImplTest : AutoCloseKoinTest() {

    private val holderDatabase: HolderDatabase = mockk()
    private val stringDataZipper: StringDataZipper = mockk()
    private val moshi = Moshi.Builder().build()

    @Test
    fun `event group entity exports to migration parcels`() = runTest {
        coEvery { holderDatabase.eventGroupDao().getAll() } returns listOf(
            EventGroupEntity(
                id = 1,
                walletId = 1,
                providerIdentifier = "ZZZ",
                type = OriginType.Vaccination,
                scope = "",
                draft = false,
                expiryDate = null,
                jsonData = "".toByteArray()
            )
        )

        coEvery { stringDataZipper.zip(any()) } returns "Xi8RQlrQWV4nfZbaox3KL3vjVU6x8Dh5qr20Unao3LN74ckv6mHXDNglCH/UXeefnNQxg52y6/bl0rPuEiUdR+P5hCgu2uTBFhvArKjCfXOqYeRVSDzpyY9zyJAUTcQdzA0OmHFlUEf1R7nQXOaj4lgvZdQEHMucSrOvsPzgTuEekYy0Nzb1wG6in0jVfT4rXLjatVR3fl4vUpSRP8olKVWuGUP0cXLlSe4maxy/U1jvM0UFh3E4C3PiEc1Sd2a60wcjpKijeUdxfadcvpRC3szAFmMTdkQs5XudkM42OvLxJUYw/wkqFhum1pJCPfws17t6jzdnMU8nhONqIXcaz68s0vdLPEuZYyeLEMwx1JCa9GnBwQkZl0u6g/GTZ0wPpOvuJtdP7szlCTXKXKeIKKmJs72XsW21aqjMb8ulnyyRB7dAErHW3Gmb5EeEc3Pdzlm2fSltI6o26FIkHybePkVOn3IxmkSYicWeMqcBfZimWo0PMrCCx/6GiuZ4QrpUcWNJf9icntsjDYMwg75wh9pssDZDyWsh5ZmKbVQivCvtzTVS9kriU61m0IkaFWKztiEyLiYmGl/OYfB6kUynQxDpIVhMK7M6m07EaT4Zpu7JwXM6/HimVu9O7kyhjrjqHkgsMsu6+sPYIL2IRiic3BkKntSdPe1gW6PGrzHVxkmGCiUqlTWsSbS/2OZdRbMSXaOy7HOu+u1r6iZWicIz2TYw++BOUdBqORbpMN5ZxBBD45N0FNZv52oxnmk+5kZss0tvY6jNE1mCEx1Mdt4bLRULin4qtGPu".toByteArray()

        val dataExportUseCase = DataExportUseCaseImpl(holderDatabase, moshi, stringDataZipper)

        val migrationParcels = dataExportUseCase.export()

        assertEquals(2, migrationParcels.size)
    }
}
