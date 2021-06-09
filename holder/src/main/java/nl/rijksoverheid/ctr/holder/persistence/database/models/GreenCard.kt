package nl.rijksoverheid.ctr.holder.persistence.database.models

import androidx.room.Embedded
import androidx.room.Relation
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class GreenCard(
    @Embedded val greenCardEntity: GreenCardEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "green_card_id"
    )
    val origins: List<OriginEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "green_card_id"
    )
    val credentialEntities: List<CredentialEntity>,
)
