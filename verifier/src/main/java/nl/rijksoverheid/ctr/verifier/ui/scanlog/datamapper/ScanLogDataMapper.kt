package nl.rijksoverheid.ctr.verifier.ui.scanlog.datamapper

import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLog
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLogBuilder
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScanLogDataMapper {
    fun transform(entities: List<ScanLogEntity>): List<ScanLog>
}

class ScanLogDataMapperImpl: ScanLogDataMapper {
    override fun transform(entities: List<ScanLogEntity>): List<ScanLog> {
        var currentTime: Instant? = null
        var currentScanLogBuilder: ScanLogBuilder? = null
        val scanLogs = mutableListOf<ScanLog>()

        entities.forEach { entity ->
            if (currentScanLogBuilder == null || entity.policy != currentScanLogBuilder?.policy || currentTime?.isAfter(entity.date) == true) {
                currentScanLogBuilder?.let {
                    scanLogs.add(it.build())
                }
                currentScanLogBuilder = ScanLogBuilder()
                currentScanLogBuilder?.policy = entity.policy
                if (currentTime?.isAfter(entity.date) == true) {
                    currentScanLogBuilder?.skew = true
                }
            }
            currentTime = entity.date
            currentScanLogBuilder?.count = (currentScanLogBuilder?.count ?: 0) + 1

            if (currentScanLogBuilder?.to == null) {
                currentScanLogBuilder?.to = OffsetDateTime.ofInstant(entity.date, ZoneId.of("UTC"))
            } else {
                currentScanLogBuilder?.to = listOfNotNull(
                    currentScanLogBuilder?.to,
                    OffsetDateTime.ofInstant(entity.date, ZoneId.of("UTC"))
                ).maxOf { it }
            }

            if (currentScanLogBuilder?.from == null) {
                currentScanLogBuilder?.from = OffsetDateTime.ofInstant(entity.date, ZoneId.of("UTC"))
            } else {
                currentScanLogBuilder?.to = listOfNotNull(
                    currentScanLogBuilder?.to,
                    OffsetDateTime.ofInstant(entity.date, ZoneId.of("UTC"))
                ).minOf { it }
            }
        }

        currentScanLogBuilder?.let {
            scanLogs.add(it.build())
        }

        return scanLogs.reversed()
    }
}