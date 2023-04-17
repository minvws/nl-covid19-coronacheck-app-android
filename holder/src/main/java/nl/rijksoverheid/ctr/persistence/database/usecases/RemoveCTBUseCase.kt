/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.persistence.database.usecases

import android.util.Base64
import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.holder.your_events.utils.SignedResponse
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType

interface RemoveCTBUseCase {
    suspend fun execute()
}

class RemoveCTBUseCaseImpl(
    private val moshi: Moshi,
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
        val remainingEventGroups = holderDatabase.eventGroupDao().getAll()
        val eventGroupsWithVaccinationAssessmentEvents = remainingEventGroups.filter {
            val payload = moshi.adapter(SignedResponse::class.java)
                .fromJson(String(it.jsonData))?.payload
            String(Base64.decode(payload, Base64.DEFAULT)).contains("vaccinationassessment")
        }
        if (eventGroupsWithVaccinationAssessmentEvents.isNotEmpty()) {
            holderDatabase.eventGroupDao().deleteAllOfIds(eventGroupsWithVaccinationAssessmentEvents.map { it.id })
        }
        holderDatabase.greenCardDao().deleteAllOfNotTypes(
            listOf(
                GreenCardType.Eu
            )
        )
    }
}
