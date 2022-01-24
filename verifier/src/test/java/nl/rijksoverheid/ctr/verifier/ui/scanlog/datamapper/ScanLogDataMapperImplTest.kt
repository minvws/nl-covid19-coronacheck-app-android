package nl.rijksoverheid.ctr.verifier.ui.scanlog.datamapper

import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.verifier.persistance.database.entities.ScanLogEntity
import nl.rijksoverheid.ctr.verifier.ui.scanlog.models.ScanLog
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class ScanLogDataMapperImplTest {

    @Test
    fun `transform maps correct objects`() {
        val dataMapper = ScanLogDataMapperImpl()

        val entities = listOf(
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy1G,
                date = Instant.parse("2021-01-01T00:00:00.00Z")
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy1G,
                date = Instant.parse("2021-01-01T00:05:00.00Z")
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy1G,
                date = Instant.parse("2021-01-01T00:10:00.00Z")
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy3G,
                date = Instant.parse("2021-01-01T00:15:00.00Z")
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy3G,
                date = Instant.parse("2021-01-01T00:12:00.00Z")
            ),
            ScanLogEntity(
                policy = VerificationPolicy.VerificationPolicy1G,
                date = Instant.parse("2021-01-01T00:20:00.00Z")
            ),
        )

        val expectedModels = listOf(
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy1G,
                count = 1,
                skew = false,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:20:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:20:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy3G,
                count = 1,
                skew = true,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:12:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:12:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy3G,
                count = 1,
                skew = false,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:15:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:15:00.00Z"),
                    ZoneId.of("UTC")
                )
            ),
            ScanLog(
                policy = VerificationPolicy.VerificationPolicy1G,
                count = 3,
                skew = false,
                from = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:00:00.00Z"),
                    ZoneId.of("UTC")
                ),
                to = OffsetDateTime.ofInstant(
                    Instant.parse("2021-01-01T00:10:00.00Z"),
                    ZoneId.of("UTC")
                )
            )
        )

        assertEquals(expectedModels, dataMapper.transform(entities))
    }
}