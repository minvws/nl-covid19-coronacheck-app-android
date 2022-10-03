package nl.rijksoverheid.ctr.verifier.persistance.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierDatabaseConverters {
    @TypeConverter
    fun fromTimestampToInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun instantToTimestamp(instant: Instant?): Long? {
        return instant?.toEpochMilli()
    }

    @TypeConverter
    fun fromStringToVerificationPolicy(value: String?): VerificationPolicy? {
        return value?.let { VerificationPolicy.fromString(it) }
    }

    @TypeConverter
    fun fromVerificationPolicyToString(policy: VerificationPolicy?): String? {
        return policy?.libraryValue
    }
}
