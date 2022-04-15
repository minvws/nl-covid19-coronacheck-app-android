package nl.rijksoverheid.ctr.persistence.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import nl.rijksoverheid.ctr.persistence.database.converters.HolderDatabaseConverter
import nl.rijksoverheid.ctr.persistence.database.dao.*
import nl.rijksoverheid.ctr.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.usecases.SecretKeyUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE origin ADD COLUMN doseNumber INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE credential ADD COLUMN category VARCHAR")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE event_group ADD COLUMN scope TEXT")
        database.execSQL("DROP INDEX index_event_group_provider_identifier_type")
        database.execSQL("CREATE UNIQUE INDEX index_event_group_provider_identifier_type_scope ON event_group(provider_identifier, type, scope)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // create new table for event group with scope not null
        database.execSQL("CREATE TABLE IF NOT EXISTS event_group_temp (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, wallet_id INTEGER NOT NULL, provider_identifier TEXT NOT NULL, type TEXT NOT NULL, scope TEXT NOT NULL, maxIssuedAt INTEGER NOT NULL, jsonData BLOB NOT NULL, FOREIGN KEY(wallet_id) REFERENCES wallet(id) ON UPDATE NO ACTION ON DELETE CASCADE )")

        // update existing null scope values to empty strings
        database.execSQL("UPDATE event_group SET scope = '' WHERE scope IS NULL")

        // copy data from old event group table to new one
        database.execSQL("INSERT INTO event_group_temp (id, wallet_id, provider_identifier, type, scope, maxIssuedAt, jsonData) SELECT id, wallet_id, provider_identifier, type, scope, maxIssuedAt, jsonData FROM event_group")

        // delete old event group table and index
        database.execSQL("DROP TABLE IF EXISTS event_group")
        database.execSQL("DROP INDEX IF EXISTS index_event_group_provider_identifier_type_scope")
        database.execSQL("DROP INDEX IF EXISTS index_event_group_wallet_id")

        // rename new table and set index
        database.execSQL("ALTER TABLE event_group_temp RENAME TO event_group")
        database.execSQL("CREATE INDEX index_event_group_wallet_id ON event_group(wallet_id)")
        database.execSQL("CREATE UNIQUE INDEX index_event_group_provider_identifier_type_scope ON event_group(provider_identifier, type, scope)")
    }
}

@Database(
    entities = [WalletEntity::class, EventGroupEntity::class, GreenCardEntity::class, CredentialEntity::class, OriginEntity::class],
    version = 5
)
@TypeConverters(HolderDatabaseConverter::class)
abstract class HolderDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun greenCardDao(): GreenCardDao
    abstract fun credentialDao(): CredentialDao
    abstract fun eventGroupDao(): EventGroupDao
    abstract fun originDao(): OriginDao

    companion object {
        fun createInstance(context: Context, secretKeyUseCase: SecretKeyUseCase, isProd: Boolean = true): HolderDatabase {
            val supportFactory =
                SupportFactory(SQLiteDatabase.getBytes(secretKeyUseCase.json().toCharArray()))
            return Room
                .databaseBuilder(context, HolderDatabase::class.java, "holder-database")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .apply {
                    if (isProd) {
                        openHelperFactory(supportFactory)
                    }
                }.build()
        }
    }
}
