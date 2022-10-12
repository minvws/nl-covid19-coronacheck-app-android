package nl.rijksoverheid.ctr.persistence.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.entities.RemovedEventReason

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
    fun fromGreenCardType(value: String?): GreenCardType? {
        return when (value) {
            GreenCardType.TYPE_DOMESTIC -> GreenCardType.Domestic
            GreenCardType.TYPE_EU -> GreenCardType.Eu
            else -> null
        }
    }

    @TypeConverter
    fun greenCardTypeToString(type: GreenCardType?): String? {
        return when (type) {
            GreenCardType.Domestic -> GreenCardType.TYPE_DOMESTIC
            GreenCardType.Eu -> GreenCardType.TYPE_EU
            else -> null
        }
    }

    @TypeConverter
    fun fromOriginType(value: String?): OriginType? {
        return when (value) {
            OriginType.TYPE_RECOVERY -> OriginType.Recovery
            OriginType.TYPE_TEST -> OriginType.Test
            OriginType.TYPE_VACCINATION -> OriginType.Vaccination
            OriginType.TYPE_VACCINATION_ASSESSMENT -> OriginType.VaccinationAssessment
            else -> null
        }
    }

    @TypeConverter
    fun originTypeToString(type: OriginType?): String? {
        return when (type) {
            OriginType.Recovery -> OriginType.TYPE_RECOVERY
            OriginType.Test -> OriginType.TYPE_TEST
            OriginType.Vaccination -> OriginType.TYPE_VACCINATION
            OriginType.VaccinationAssessment -> OriginType.TYPE_VACCINATION_ASSESSMENT
            else -> null
        }
    }

    @TypeConverter
    fun fromRemovedEventReason(value: String): RemovedEventReason {
        return when (value) {
            RemovedEventReason.FUZZY_MATCHED -> RemovedEventReason.FuzzyMatched
            else -> RemovedEventReason.Blocked
        }
    }

    @TypeConverter
    fun removedEventReasonToString(type: RemovedEventReason): String {
        return when (type) {
            RemovedEventReason.Blocked -> RemovedEventReason.BLOCKED
            RemovedEventReason.FuzzyMatched -> RemovedEventReason.FUZZY_MATCHED
        }
    }
}
