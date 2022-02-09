package nl.rijksoverheid.ctr.verifier.persistance.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import nl.rijksoverheid.ctr.verifier.persistance.database.converters.VerifierDatabaseConverters
import nl.rijksoverheid.ctr.verifier.persistance.database.dao.ScanLogDao
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.persistance.usecase.RandomKeyUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Database(
    entities = [ScanLogEntity::class],
    version = 1
)
@TypeConverters(VerifierDatabaseConverters::class)
abstract class VerifierDatabase : RoomDatabase() {
    abstract fun scanLogDao(): ScanLogDao

    companion object {
        fun createInstance(
            context: Context,
            randomKeyUseCase: RandomKeyUseCase,
            isProd: Boolean = true
        ): VerifierDatabase {
            val supportFactory =
                SupportFactory(SQLiteDatabase.getBytes(randomKeyUseCase.get().toCharArray()))
            return Room
                .databaseBuilder(context, VerifierDatabase::class.java, "verifier-database")
                .apply {
                    if (isProd) {
                        openHelperFactory(supportFactory)
                    }
                }.build()
        }
    }
}