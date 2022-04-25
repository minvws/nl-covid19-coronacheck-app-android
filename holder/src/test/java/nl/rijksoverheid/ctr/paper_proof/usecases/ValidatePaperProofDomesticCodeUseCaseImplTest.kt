package nl.rijksoverheid.ctr.paper_proof.usecases

import nl.rijksoverheid.ctr.holder.paper_proof.models.PaperProofDomesticCodeResult
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticCodeUseCaseImpl
import org.junit.Test
import kotlin.test.assertEquals

class ValidatePaperProofDomesticCodeUseCaseImplTest {

    private val usecase = ValidatePaperProofDomesticCodeUseCaseImpl()

    @Test
    fun `Empty code returns Empty`() {
        assertEquals(PaperProofDomesticCodeResult.Empty, usecase.validate(""))
    }

    @Test
    fun `Code with more less than six chars returns Invalid`() {
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
}