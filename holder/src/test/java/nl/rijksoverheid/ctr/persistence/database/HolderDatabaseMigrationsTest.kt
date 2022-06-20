package nl.rijksoverheid.ctr.persistence.database

import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class HolderDatabaseMigrationsTest: AutoCloseKoinTest() {

    private val persistenceManager: PersistenceManager by inject()

    companion object {
        private const val DATABASE_NAME = "DATABASE_NAME"
    }

    @get:Rule
    val helper: RobolectricMigrationTestHelper = RobolectricMigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
        HolderDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory())

    @Test
    fun testMigration1To2() {
        // Our database before the migration
        val dbV1 = helper.createDatabase(DATABASE_NAME, 1)
        dbV1.close()

        // The database after the migration
        val dbV2 = helper.runMigrationsAndValidate(DATABASE_NAME, 2, true, MIGRATION_1_2)

        // Assert no errors
        val cursor = dbV2.query("SELECT * FROM green_card")
        assertNotNull(cursor)
    }

    @Test
    fun testMigration3To4() {
        // Our database before the migration
        val dbV1 = helper.createDatabase(DATABASE_NAME, 3)
        dbV1.close()

        // The database after the migration
        val dbV2 = helper.runMigrationsAndValidate(DATABASE_NAME, 4, true, MIGRATION_3_4)

        // Assert no errors
        val cursor = dbV2.query("SELECT * FROM event_group")
        assertNotNull(cursor)
    }

    @Test
    fun testMigration6To7() {
        // Our database before the migration
        val dbV6 = helper.createDatabase(DATABASE_NAME, 6)
        dbV6.close()

        // The database after the migration
        val dbV7 = helper.runMigrationsAndValidate(DATABASE_NAME, 7, true, MIGRATION_6_7(persistenceManager, "123"))

        // Assert no errors
        val cursor = dbV7.query("SELECT * FROM event_group")
        assertNotNull(cursor)

        // Assert new secret key is stored in preference
        assertEquals("123", persistenceManager.getDatabasePassPhrase())
    }
}