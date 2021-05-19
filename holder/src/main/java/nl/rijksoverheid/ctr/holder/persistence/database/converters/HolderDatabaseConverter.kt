package nl.rijksoverheid.ctr.holder.persistence.database.converters

import androidx.room.TypeConverter
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
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
class HolderDatabaseConverter {
    @TypeConverter
    fun fromTimestampToOffsetDateTime(value: Long?): OffsetDateTime? {
        return value?.let { OffsetDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun offsetDateTimeToTimestamp(date: OffsetDateTime?): Long? {
        return date?.toEpochSecond()
    }

    @TypeConverter
    fun fromEventType(value: String?): EventType? {
        return when (value) {
            EventType.TYPE_RECOVERY -> EventType.Recovery
            EventType.TYPE_TEST -> EventType.Test
            EventType.TYPE_VACCINATION -> EventType.Vaccination
            else -> null
        }
    }

    @TypeConverter
    fun fromGreenCardType(value: String?): GreenCardType? {
        return when (value) {
            GreenCardType.TYPE_DOMESTIC -> GreenCardType.Domestic
            GreenCardType.TYPE_EU_ALL_IN_ONE -> GreenCardType.EuAllInOne
            GreenCardType.TYPE_EU_RECOVERY -> GreenCardType.EuRecovery
            GreenCardType.TYPE_EU_TEST -> GreenCardType.EuTest
            GreenCardType.TYPE_EU_VACCINATION -> GreenCardType.EuVaccination
            else -> null
        }
    }

    @TypeConverter
    fun eventTypeToString(type: EventType?): String? {
        return when (type) {
            EventType.Recovery -> EventType.TYPE_RECOVERY
            EventType.Test -> EventType.TYPE_TEST
            EventType.Vaccination -> EventType.TYPE_VACCINATION
            else -> null
        }
    }

    @TypeConverter
    fun greenCardTypeToString(type: GreenCardType?): String? {
        return when (type) {
            GreenCardType.Domestic -> GreenCardType.TYPE_DOMESTIC
            GreenCardType.EuAllInOne -> GreenCardType.TYPE_EU_ALL_IN_ONE
            GreenCardType.EuRecovery -> GreenCardType.TYPE_EU_RECOVERY
            GreenCardType.EuTest -> GreenCardType.TYPE_EU_TEST
            GreenCardType.EuVaccination -> GreenCardType.TYPE_EU_VACCINATION
            else -> null
        }
    }
}
