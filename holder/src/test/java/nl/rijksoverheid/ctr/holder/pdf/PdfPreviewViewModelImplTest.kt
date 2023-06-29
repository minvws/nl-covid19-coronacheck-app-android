package nl.rijksoverheid.ctr.holder.pdf

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PdfPreviewViewModelImplTest {

    private val previewPdfUseCase = mockk<PreviewPdfUseCase>()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `when generatePreview returns info then post success`() {
        coEvery {
            previewPdfUseCase.generatePreview(
                any(),
                any()
            )
        } returns PDfPreviewInfo("content", 1)
        val viewModel = PdfPreviewViewModelImpl(previewPdfUseCase)

        viewModel.generatePreview(1020, mockk())

        val pdfPreview = viewModel.previewLiveData.value?.peekContent() as PdfPreview.Success
        assertEquals(1, pdfPreview.info.initialZoom)
        assertEquals("content", pdfPreview.info.content)
    }

    @Test
    fun `when generatePreview returns null then post error`() {
        coEvery { previewPdfUseCase.generatePreview(any(), any()) } returns null
        val viewModel = PdfPreviewViewModelImpl(previewPdfUseCase)

        viewModel.generatePreview(1020, mockk())

        assertTrue(viewModel.previewLiveData.value?.peekContent() is PdfPreview.Error)
    }
}
