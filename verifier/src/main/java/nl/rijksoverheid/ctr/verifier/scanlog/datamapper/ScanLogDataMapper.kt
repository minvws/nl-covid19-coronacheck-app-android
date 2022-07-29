package nl.rijksoverheid.ctr.verifier.scanlog.datamapper

import java.time.Instant
import nl.rijksoverheid.ctr.design.ext.toOffsetDateTimeUtc
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.scanlog.models.ScanLog
import nl.rijksoverheid.ctr.verifier.scanlog.models.ScanLogBuilder

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

class ScanLogDataMapperImpl : ScanLogDataMapper {
    override fun transform(entities: List<ScanLogEntity>): List<ScanLog> {
        var currentTime: Instant? = null
        var currentScanLogBuilder: ScanLogBuilder? = null
        val scanLogs = mutableListOf<ScanLog>()

        entities.forEach { entity ->
            if (currentScanLogBuilder == null || entity.policy != currentScanLogBuilder?.policy || currentTime?.isAfter(entity.date) == true) {
                currentScanLogBuilder?.let { builder ->
                    // Show logging as continuous by setting the to date to when the the first scan happens after policy switch
                    if (!builder.skew) builder.to = entity.date.toOffsetDateTimeUtc()

                    scanLogs.add(builder.build())
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
                currentScanLogBuilder?.to = entity.date.toOffsetDateTimeUtc()
            } else {
                currentScanLogBuilder?.to = listOfNotNull(
                    currentScanLogBuilder?.to,
                    entity.date.toOffsetDateTimeUtc()
                ).maxOf { it }
            }

            if (currentScanLogBuilder?.from == null) {
                currentScanLogBuilder?.from = entity.date.toOffsetDateTimeUtc()
            } else {
                currentScanLogBuilder?.to = listOfNotNull(
                    currentScanLogBuilder?.to,
                    entity.date.toOffsetDateTimeUtc()
                ).minOf { it }
            }
        }

        currentScanLogBuilder?.let {
            scanLogs.add(it.build())
        }

        return scanLogs.reversed()
    }
}
