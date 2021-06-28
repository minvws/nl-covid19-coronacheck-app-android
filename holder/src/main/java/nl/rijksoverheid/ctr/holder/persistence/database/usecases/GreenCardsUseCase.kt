/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.persistence.database.usecases

import nl.rijksoverheid.ctr.appconfig.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

interface GreenCardsUseCase {
    suspend fun faultyVaccinations(): Boolean
}

class GreenCardsUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase,
    private val greenCardUtil: GreenCardUtil,
    private val clock: Clock,
): GreenCardsUseCase {

    private val bugDate = OffsetDateTime.ofInstant(
        Instant.parse("2021-06-28T09:00:00.00Z"),
        ZoneId.of("UTC")
    )

    override suspend fun faultyVaccinations(): Boolean {
        return holderDatabase.greenCardDao().getAll().filter { it.greenCardEntity.type == GreenCardType.Domestic }
            .any { greenCard ->
                val hasVaccinationAndTestOrigins = greenCard.origins.map { it.type }.containsAll(setOf(
                    OriginType.Test, OriginType.Vaccination))
                val originsOlderThanBugDate = greenCard.origins.any { it.eventTime.isBefore(bugDate) }

                hasVaccinationAndTestOrigins && originsOlderThanBugDate
            }
    }
}