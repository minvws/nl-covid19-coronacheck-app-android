package nl.rijksoverheid.ctr.verifier.persistance.database.converters

import androidx.room.TypeConverter
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import java.time.Instant

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
    fun fromStringToVerificationPolicy(value: Long?): VerificationPolicy? {
        return value?.let { VerificationPolicy.fromLibraryValue(it) }
    }

    @TypeConverter
    fun fromVerificationPolicyToString(policy: VerificationPolicy?): Long? {
        return policy?.libraryValue
    }
}