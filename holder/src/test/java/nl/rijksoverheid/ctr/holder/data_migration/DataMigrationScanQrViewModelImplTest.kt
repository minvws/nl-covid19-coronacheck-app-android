package nl.rijksoverheid.ctr.holder.data_migration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.getOrAwaitValue
import nl.rijksoverheid.ctr.holder.get_events.usecases.ConfigProvidersUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.EventProvidersResult
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationDecodingErrorException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationInvalidNumberOfPackagesException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationInvalidVersionException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationOtherException
import nl.rijksoverheid.ctr.shared.exceptions.NoProvidersException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataMigrationScanQrViewModelImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataMigrationImportUseCase: DataMigrationImportUseCase = mockk()
    private val dataMigrationPayloadUseCase: DataMigrationPayloadUseCase = mockk()
    private val configProvidersUseCase: ConfigProvidersUseCase = mockk()

    private val viewModel = DataMigrationScanQrViewModelImpl(
        dataMigrationImportUseCase,
        dataMigrationPayloadUseCase,
        configProvidersUseCase
    )

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `given a qr that throws exception when importing it, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } throws Exception("Import failed")

        viewModel.onQrScanned("qrcodecontent")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is DataMigrationDecodingErrorException)
    }

    @Test
    fun `given a qr that generates no migration parcel, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns null

        viewModel.onQrScanned("qrcodecontent")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is DataMigrationOtherException)
    }

    @Test
    fun `given a qr that generates a migration parcel with different version, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns mockk<MigrationParcel>().apply {
            coEvery { version } returns "CC2"
        }

        viewModel.onQrScanned("qrcodecontent")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is DataMigrationInvalidVersionException)
    }

    @Test
    fun `given a qr that generates a valid migration parcel, then app state updates progress`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns MigrationParcel(
            index = 1,
            numberOfPackages = 10,
            payload = "",
            version = "CC1"
        )

        viewModel.onQrScanned("qrcodecontent")

        val progressBarLiveData = viewModel.progressBarLiveData.getOrAwaitValue()

        assertEquals(10, progressBarLiveData.calculateProgressPercentage())
    }

    @Test
    fun `given a qr that generates a migration parcel with bigger number of packages, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returnsMany listOf(
            MigrationParcel(
                index = 1,
                numberOfPackages = 10,
                payload = "",
                version = "CC1"
            ),
            MigrationParcel(
                index = 1,
                numberOfPackages = 1,
                payload = "",
                version = "CC1"
            )
        )

        viewModel.onQrScanned("qrcodecontent1")
        viewModel.onQrScanned("qrcodecontent2")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is DataMigrationInvalidNumberOfPackagesException)
    }

    @Test
    fun `given a qr that generates a migration parcel that fails to merge to an event parcel, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns MigrationParcel(
            index = 1,
            numberOfPackages = 1,
            payload = "",
            version = "CC1"
        )
        coEvery { dataMigrationImportUseCase.merge(any()) } throws Exception("exception")

        viewModel.onQrScanned("qrcodecontent1")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is DataMigrationDecodingErrorException)
    }

    @Test
    fun `given a qr that generates a migration parcel but we have no event providers, then app state is DataMigrationImport error`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns MigrationParcel(
            index = 1,
            numberOfPackages = 1,
            payload = "",
            version = "CC1"
        )
        coEvery { dataMigrationImportUseCase.merge(any()) } returns listOf(mockk<EventGroupParcel>().apply {
            coEvery { jsonData } returns "".toByteArray()
        })
        coEvery { dataMigrationPayloadUseCase.parsePayload(any()) } returns mockk()
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Error(mockk())

        viewModel.onQrScanned("qrcodecontent1")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Error)
        assertTrue(dataMigrationScanQrState.errorResult.e is NoProvidersException.Migration)
    }

    @Test
    fun `given a qr that generated all migrations parcels, then app state is Success`() {
        coEvery { dataMigrationImportUseCase.import(any()) } returns MigrationParcel(
            index = 1,
            numberOfPackages = 1,
            payload = "",
            version = "CC1"
        )
        coEvery { dataMigrationImportUseCase.merge(any()) } returns listOf(mockk<EventGroupParcel>().apply {
            coEvery { jsonData } returns "".toByteArray()
        })
        coEvery { dataMigrationPayloadUseCase.parsePayload(any()) } returns mockk()
        coEvery { configProvidersUseCase.eventProviders() } returns EventProvidersResult.Success(
            listOf()
        )

        viewModel.onQrScanned("qrcodecontent1")

        val dataMigrationScanQrState =
            viewModel.scanFinishedLiveData.getOrAwaitValue().peekContent()

        assertTrue(dataMigrationScanQrState is DataMigrationScanQrState.Success)
    }
}
