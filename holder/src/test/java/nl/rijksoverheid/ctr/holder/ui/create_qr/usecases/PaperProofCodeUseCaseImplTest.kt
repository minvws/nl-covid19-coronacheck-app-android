package nl.rijksoverheid.ctr.holder.ui.create_qr.usecases

import nl.rijksoverheid.ctr.holder.ui.create_qr.models.PaperProofCodeResult
import org.junit.Test
import kotlin.test.assertEquals

class PaperProofCodeUseCaseImplTest {

    private val usecase = PaperProofCodeUseCaseImpl()

    @Test
    fun `Code with less than six chars returns NotSixCharacters`() {
        assertEquals(PaperProofCodeResult.NotSixCharacters, usecase.validate("234"))
    }

    @Test
    fun `Code with more than six chars returns NotSixCharacters`() {
        assertEquals(PaperProofCodeResult.NotSixCharacters, usecase.validate("234234234"))
    }

    @Test
    fun `Code with invalid chars returns Invalid`() {
        assertEquals(PaperProofCodeResult.Invalid, usecase.validate("111111"))
    }

    @Test
    fun `Code with valid 6 chars returns Valid`() {
        assertEquals(PaperProofCodeResult.Valid, usecase.validate("222222"))
    }
}