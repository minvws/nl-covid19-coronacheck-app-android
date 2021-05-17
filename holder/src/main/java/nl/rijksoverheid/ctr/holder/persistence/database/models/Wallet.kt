package nl.rijksoverheid.ctr.holder.persistence.database.models

import androidx.room.Embedded
import androidx.room.Relation
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.WalletEntity

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
data class Wallet(
    @Embedded val walletEntity: WalletEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "wallet_id",
        entity = EventGroupEntity::class
    )
    val eventEntities: List<EventGroupEntity> = listOf(),

    @Relation(
        parentColumn = "id",
        entityColumn = "wallet_id",
        entity = GreenCardEntity::class
    )
    val greenCards: List<GreenCard> = listOf()
)
