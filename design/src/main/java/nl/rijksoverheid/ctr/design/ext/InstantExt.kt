package nl.rijksoverheid.ctr.design.ext

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

fun Instant.toOffsetDateTimeUtc(): OffsetDateTime {
    return OffsetDateTime.ofInstant(this, ZoneId.of("UTC"))
}