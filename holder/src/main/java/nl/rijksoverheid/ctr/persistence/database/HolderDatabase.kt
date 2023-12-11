package nl.rijksoverheid.ctr.persistence.database

import android.content.ContentValues
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SupportFactory
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.database.converters.HolderDatabaseConverter
import nl.rijksoverheid.ctr.persistence.database.dao.CredentialDao
import nl.rijksoverheid.ctr.persistence.database.dao.EventGroupDao
import nl.rijksoverheid.ctr.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.persistence.database.dao.OriginDao
import nl.rijksoverheid.ctr.persistence.database.dao.OriginHintDao
import nl.rijksoverheid.ctr.persistence.database.dao.RemovedEventDao
import nl.rijksoverheid.ctr.persistence.database.dao.SecretKeyDao
import nl.rijksoverheid.ctr.persistence.database.dao.WalletDao
import nl.rijksoverheid.ctr.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginHintEntity
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventEntity
import nl.rijksoverheid.ctr.persistence.database.entities.SecretKeyEntity
import nl.rijksoverheid.ctr.persistence.database.entities.WalletEntity
import nl.rijksoverheid.ctr.shared.models.Environment
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

@TypeConverters(HolderDatabaseConverter::class)
abstract class HolderDatabase : RoomDatabase() {

    companion object {
        fun deleteDatabase(
            context: Context
        ) {
            try {
                val file = File(context.filesDir.parentFile, "databases/holder-database")
                file.delete()
            } catch (e: Exception) {
                // no op
            }
        }
    }
}
