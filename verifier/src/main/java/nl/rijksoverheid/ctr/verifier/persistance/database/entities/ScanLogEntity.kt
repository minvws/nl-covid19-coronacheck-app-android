package nl.rijksoverheid.ctr.verifier.persistance.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import java.time.Instant

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Entity(
    tableName = "scan_log",
)
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val policy: VerificationPolicy,
    val date: Instant
)