package nl.rijksoverheid.ctr.appconfig.persistence

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test

class AppConfigStorageManagerImplTest {
    private val appConfigStorageManager = AppConfigStorageManagerImpl("")

    @Test
    fun `getFileAsBufferedSource reads file and closes it properly`() {
        val filePath = javaClass.classLoader?.getResource("config.json")!!.path
        val file = File(filePath)

        val contents = appConfigStorageManager.getFileAsBufferedSource(file)

        assertEquals("{}", contents)
    }
}
