package nl.rijksoverheid.ctr.holder.your_events.utils

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment.getApplication
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class RemoteEventStringUtilImplTest(
    private val param: Class<out RemoteEvent>,
    private val expected: String
) : AutoCloseKoinTest() {
    private val remoteEventUtil = RemoteEventStringUtilImpl(getApplication()::getString)

    @Test
    fun `remoteEventTitle returns correct string`() {
        assertEquals(expected, remoteEventUtil.remoteEventTitle(param))
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data() = listOf(
            arrayOf(RemoteEventVaccination::class.java, "Vaccinatie"),
            arrayOf(RemoteEventNegativeTest::class.java, "Negatieve testuitslag"),
            arrayOf(RemoteEventPositiveTest::class.java, "Positieve testuitslag"),
            arrayOf(RemoteEventRecovery::class.java, "Herstelbewijs"),
            arrayOf(RemoteEvent::class.java, "")
        )
    }
}
