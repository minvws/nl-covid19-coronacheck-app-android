package nl.rijksoverheid.ctr.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofCodeResult
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.PaperProofCodeUseCaseImpl
import org.junit.Test
import kotlin.test.assertEquals

class PaperProofCodeUseCaseImplTest {

    private val usecase = PaperProofCodeUseCaseImpl()

    @Test
    fun `Empty code returns Empty`() {
        assertEquals(PaperProofCodeResult.Empty, usecase.validate(""))
    }

    @Test
    fun `Code with more less than six chars returns Invalid`() {
        assertEquals(PaperProofCodeResult.Invalid, usecase.validate("234"))
    }

    @Test
    fun `Code with more than six chars returns Invalid`() {
        assertEquals(PaperProofCodeResult.Invalid, usecase.validate("234234234"))
    }

    @Test
    fun `Code with valid 6 chars returns Valid`() {
        assertEquals(PaperProofCodeResult.Valid, usecase.validate("222222"))
    }
}