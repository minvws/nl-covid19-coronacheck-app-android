package nl.rijksoverheid.ctr.persistence.database

import android.content.ContentValues
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HolderDatabaseMigrationsTest : AutoCloseKoinTest() {

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
    fun `Database v6 to v7 migration migrates successfully with existing domestic green card`() {
        // Add a secret to to our preferences to migrate
        persistenceManager.saveDatabasePassPhrase("123456789")

        // Our database before the migration
        val dbV6 = helper.createDatabase(DATABASE_NAME, 6)

        // Insert green card entity
        val insertValues = ContentValues()
        insertValues.put("id", 1)
        insertValues.put("wallet_id", 1)
        insertValues.put("type", "domestic")
        dbV6.insert("green_card", 0, insertValues)

        dbV6.close()

        // The database after the migration
        val dbV7 = helper.runMigrationsAndValidate(DATABASE_NAME, 7, true, MIGRATION_6_7(persistenceManager))

        // Assert new secret key is stored in preference
        assertEquals("123456789", persistenceManager.getDatabasePassPhrase())

        // Assert that the secret key from shared preferences is migrated to secret key table
        val secretKeyCursor = dbV7.query("SELECT * FROM secret_key")
        secretKeyCursor.moveToFirst()
        assertEquals(1, secretKeyCursor.count)
        assertEquals("123456789", secretKeyCursor.getString(secretKeyCursor.getColumnIndex("secret")))
    }

    @Test
    fun `Database v6 to v7 migration migrates successfully without domestic green card`() {
        // Add a secret to to our preferences to migrate
        persistenceManager.saveDatabasePassPhrase("123456789")

        // Our database before the migration
        val dbV6 = helper.createDatabase(DATABASE_NAME, 6)
        dbV6.close()

        // The database after the migration
        val dbV7 = helper.runMigrationsAndValidate(DATABASE_NAME, 7, true, MIGRATION_6_7(persistenceManager))

        // Assert new secret key is stored in preference
        assertEquals("123456789", persistenceManager.getDatabasePassPhrase())

        // Assert that the secret key from shared preferences is migrated to secret key table
        val secretKeyCursor = dbV7.query("SELECT * FROM secret_key")
        assertEquals(0, secretKeyCursor.count)
    }
}
