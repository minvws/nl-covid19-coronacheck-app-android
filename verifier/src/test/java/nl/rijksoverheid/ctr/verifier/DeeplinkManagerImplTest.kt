package nl.rijksoverheid.ctr.verifier

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeeplinkManagerImplTest {

    private lateinit var deeplinkManager: DeeplinkManager

    private fun start(introductionFinished: Boolean, appIsActive: Boolean = true) {
        val introductionPersistenceManager = mockk<IntroductionPersistenceManager>().apply {
            every { getIntroductionFinished() } returns introductionFinished
        }
        deeplinkManager =
            DeeplinkManagerImpl(introductionPersistenceManager, mockk<BuildConfigUseCase>().apply {
                every { getVersionCode() } returns 1000
            }, mockk<AppStatusUseCase>().apply {
                every { isAppActive(any()) } returns appIsActive
            })
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
    fun `given app is not active, when setting the returnUri, then you cannot get it`() {
        start(introductionFinished = true, appIsActive = false)

        deeplinkManager.set("returnUri")

        assertNull(deeplinkManager.getReturnUri())
    }

    @Test
    fun `given app is active, when setting the returnUri, then you can get it`() {
        start(introductionFinished = true, appIsActive = true)

        deeplinkManager.set("returnUri")

        assertEquals("returnUri", deeplinkManager.getReturnUri())
    }

    @Test
    fun `given introduction finished and returnUri set, when removing it, then it is removed`() {
        start(introductionFinished = true)
        deeplinkManager.set("returnUri")

        deeplinkManager.removeReturnUri()

        assertNull(deeplinkManager.getReturnUri())
    }
}