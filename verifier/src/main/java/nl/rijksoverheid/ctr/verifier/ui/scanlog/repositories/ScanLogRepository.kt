package nl.rijksoverheid.ctr.verifier.ui.scanlog.repositories

import nl.rijksoverheid.ctr.verifier.persistance.database.VerifierDatabase
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.ui.scanlog.datamapper.ScanLogDataMapper
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLog

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScanLogRepository {
    suspend fun insert(entity: ScanLogEntity)
    suspend fun getAll(): List<ScanLog>
}

class ScanLogRepositoryImpl(
    private val verifierDatabase: VerifierDatabase,
    private val scanLogDataMapper: ScanLogDataMapper
): ScanLogRepository {
    override suspend fun insert(entity: ScanLogEntity) {
        verifierDatabase.scanLogDao().insert(entity)
    }

    override suspend fun getAll(): List<ScanLog> {
        return verifierDatabase.scanLogDao().getAll().let {
            scanLogDataMapper.transform(it)
        }
    }
}