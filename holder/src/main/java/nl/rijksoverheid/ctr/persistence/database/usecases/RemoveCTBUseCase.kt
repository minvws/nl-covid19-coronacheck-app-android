/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.usecases

import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

interface RemoveCTBUseCase {
    suspend fun execute()
}

class RemoveCTBUseCaseImpl(
    private val holderDatabase: HolderDatabase
) : RemoveCTBUseCase {
    override suspend fun execute() {
        holderDatabase.eventGroupDao().deleteAllOfNotTypes(
            listOf(
                OriginType.Vaccination,
                OriginType.Recovery,
                OriginType.Test
            )
        )
        holderDatabase.greenCardDao().deleteAllOfNotTypes(
            listOf(
                GreenCardType.Eu
            )
        )
    }
}
