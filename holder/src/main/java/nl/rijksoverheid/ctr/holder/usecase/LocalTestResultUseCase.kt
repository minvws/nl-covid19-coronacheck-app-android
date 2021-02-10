package nl.rijksoverheid.ctr.holder.usecase

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.toObject
import org.threeten.bp.OffsetDateTime

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class LocalTestResultUseCase(
    private val persistenceManager: PersistenceManager,
    private val moshi: Moshi
) {

    // TODO: Check expire date
    fun get(currentDateTime: OffsetDateTime): LocalTestResult? {
        return persistenceManager.getLocalTestResultJson()?.toObject<LocalTestResult>(moshi)
    }

    fun save(credentials: String, sampleDate: OffsetDateTime) {
        val localTestResult = LocalTestResult(
            credentials = credentials,
            sampleDate = sampleDate
        )
        persistenceManager.saveLocalTestResultJson(localTestResult.toJson(moshi))
    }

}
