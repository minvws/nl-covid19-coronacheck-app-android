package nl.rijksoverheid.ctr.verifier

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import org.junit.Assert.*

import org.junit.Test

class DeeplinkManagerImplTest {

    private lateinit var deeplinkManager: DeeplinkManager

    private fun start(introductionFinished: Boolean) {
        val introductionPersistenceManager = mockk<IntroductionPersistenceManager>().apply {
            every { getIntroductionFinished() } returns introductionFinished
        }
        deeplinkManager = DeeplinkManagerImpl(introductionPersistenceManager)
    }

    @Test
    fun `given introduction has finished, when setting the returnUri, then you can get it`() {
        start(introductionFinished = true)

        deeplinkManager.set("returnUri")

        assertEquals("returnUri", deeplinkManager.getReturnUri())
    }

    @Test
    fun `given introduction not finished, when setting the returnUri, then you cannot get it yet`() {
        start(introductionFinished = false)

        deeplinkManager.set("returnUri")

        assertNull(deeplinkManager.getReturnUri())
    }

    @Test
    fun `given introduction finished and returnUri set, when removing it, then it is removed`() {
        start(introductionFinished = true)
        deeplinkManager.set("returnUri")

        deeplinkManager.removeReturnUri()

        assertNull(deeplinkManager.getReturnUri())
    }
}