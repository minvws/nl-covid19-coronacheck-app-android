package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class HolderDatabaseMigrationsTest: AutoCloseKoinTest() {

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
}