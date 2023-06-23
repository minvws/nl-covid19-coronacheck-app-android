package nl.rijksoverheid.ctr.holder.data_migration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import nl.rijksoverheid.ctr.getOrAwaitValue
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCase
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationCompressionException
import nl.rijksoverheid.ctr.shared.exceptions.DataMigrationOtherException
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DataMigrationShowQrCodeViewModelImplTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val dataExportUseCase: DataExportUseCase = mockk()
    private val qrCodeUseCase: QrCodeUseCase = mockk()

    private val viewModel = DataMigrationShowQrCodeViewModelImpl(dataExportUseCase, qrCodeUseCase)

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `generateQrCodes with no errors generates qr codes`() {
        coEvery { dataExportUseCase.export() } returns listOf("")
        coEvery { qrCodeUseCase.qrCode(any(), any(), any(), any(), any()) } returns mockk()

        viewModel.generateQrCodes(800)

        assertTrue(viewModel.qrCodesLiveData.getOrAwaitValue() is DataMigrationShowQrViewState.ShowQrs)
    }

    @Test
    fun `generateQrCodes with export error shows error`() {
        coEvery { dataExportUseCase.export() } throws IOException("")

        viewModel.generateQrCodes(800)

        val state = viewModel.qrCodesLiveData.getOrAwaitValue()

        assertTrue(
            (state as DataMigrationShowQrViewState.ShowError).errorResults.first()
                .getException() is DataMigrationCompressionException
        )
    }

    @Test
    fun `generateQrCodes with other error shows error`() {
        coEvery { dataExportUseCase.export() } throws Exception("")

        viewModel.generateQrCodes(800)

        val state = viewModel.qrCodesLiveData.getOrAwaitValue()

        assertTrue(
            (state as DataMigrationShowQrViewState.ShowError).errorResults.first()
                .getException() is DataMigrationOtherException
        )
    }
}
