package nl.rijksoverheid.ctr.holder.persistence.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nl.rijksoverheid.ctr.holder.persistence.database.converters.HolderDatabaseConverter
import nl.rijksoverheid.ctr.holder.persistence.database.dao.CredentialDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.EventDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.GreenCardDao
import nl.rijksoverheid.ctr.holder.persistence.database.dao.WalletDao
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.WalletEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Database(
    entities = [WalletEntity::class, EventEntity::class, GreenCardEntity::class, CredentialEntity::class],
    version = 1
)
@TypeConverters(HolderDatabaseConverter::class)
abstract class HolderDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun greenCardDao(): GreenCardDao
    abstract fun credentialDao(): CredentialDao
    abstract fun eventDao(): EventDao
}
