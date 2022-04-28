package nl.rijksoverheid.ctr.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticCodeResult
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticInputCodeUseCaseImpl
import org.junit.Test
import kotlin.test.assertEquals

class ValidatePaperProofDomesticInputCodeUseCaseImplTest {

    private val usecase = ValidatePaperProofDomesticInputCodeUseCaseImpl()

    @Test
    fun `Empty code returns Empty`() {
        assertEquals(PaperProofDomesticCodeResult.Empty, usecase.validate(""))
    }

    @Test
    fun `Code with less than six chars returns Invalid`() {
        assertEquals(PaperProofDomesticCodeResult.Invalid, usecase.validate("234"))
    }

    @Test
    fun `Code with more than six chars returns Invalid`() {
        assertEquals(PaperProofDomesticCodeResult.Invalid, usecase.validate("234234234"))
    }

    @Test
    fun `Code with valid 6 chars returns Valid`() {
        assertEquals(PaperProofDomesticCodeResult.Valid, usecase.validate("222222"))
    }

    @Test
    fun `Code with invalid chars returns Invalid`() {
        assertEquals(PaperProofDomesticCodeResult.Invalid, usecase.validate("aBC"))
    }
}