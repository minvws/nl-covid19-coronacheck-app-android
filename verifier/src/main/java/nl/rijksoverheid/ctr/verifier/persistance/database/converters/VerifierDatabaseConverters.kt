package nl.rijksoverheid.ctr.verifier.persistance.database.converters

import androidx.room.TypeConverter
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierDatabaseConverters {
    @TypeConverter
    fun fromTimestampToOffsetDateTime(value: Long?): OffsetDateTime? {
        return value?.let { OffsetDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun offsetDateTimeToTimestamp(date: OffsetDateTime?): Long? {
        return date?.toEpochSecond()
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