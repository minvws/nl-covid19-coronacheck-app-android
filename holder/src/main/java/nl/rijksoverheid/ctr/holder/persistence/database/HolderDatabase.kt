package nl.rijksoverheid.ctr.holder.persistence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import nl.rijksoverheid.ctr.holder.persistence.database.converters.HolderDatabaseConverter
import nl.rijksoverheid.ctr.holder.persistence.database.dao.*
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SecretKeyUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Database(
    entities = [WalletEntity::class, EventGroupEntity::class, GreenCardEntity::class, CredentialEntity::class, OriginEntity::class],
    version = 1
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
                .apply {
                    if (isProd) {
                        openHelperFactory(supportFactory)
                    }
                }.build()
        }
    }
}
