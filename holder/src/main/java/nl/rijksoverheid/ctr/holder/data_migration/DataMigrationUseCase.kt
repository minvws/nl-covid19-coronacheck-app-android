/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.ctr.holder.data_migration

import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

interface DataMigrationUseCase {
    suspend fun canTransferData(): Boolean
}

class DataMigrationUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : DataMigrationUseCase {

    override suspend fun canTransferData(): Boolean {
        val events = holderDatabase.eventGroupDao().getAll()
        return events.isNotEmpty()
    }
}
