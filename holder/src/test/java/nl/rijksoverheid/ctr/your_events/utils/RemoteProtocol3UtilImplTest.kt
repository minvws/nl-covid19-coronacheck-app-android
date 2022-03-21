package nl.rijksoverheid.ctr.your_events.utils

import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteProtocol3UtilImpl
import org.junit.Assert
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.assertEquals

class RemoteProtocol3UtilImplTest {

    private val util = RemoteProtocol3UtilImpl()

    private val clock1 = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC"))
    private val clock2 = Clock.fixed(Instant.parse("2021-07-01T00:00:00.00Z"), ZoneId.of("UTC"))

    @Test
    fun `combine one vaccination coming from two different providers`() {
        val event1 = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination())
        )

        val event2 = RemoteProtocol3(
            providerIdentifier = "RIVM",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination())
        )

        val groupedEvents = util.groupEvents(listOf(event1, event2))

        Assert.assertEquals(1, groupedEvents.keys.size)
        Assert.assertEquals(1, groupedEvents.values.size)
        Assert.assertEquals(
            "GGD, RIVM",
            groupedEvents.values.first().joinToString(", ") { it.providerIdentifier }
        )
    }

    @Test
    fun `combine two vaccinations coming from two different providers`() {
        val event1 = RemoteProtocol3(
            providerIdentifier = "RIVM",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination(
                doseNumber = "1",
                totalDoses = "2",
            ),
                vaccination(
                    doseNumber = "2",
                    totalDoses = "2",
                    clock = clock2,
                ))
        )

        val event2 = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination(
                doseNumber = "1",
                totalDoses = "2",
                clock = clock2,
            ),
                vaccination(
                    doseNumber = "2",
                    totalDoses = "2",
                ))
        )

        val groupedEvents = util.groupEvents(listOf(event1, event2))

        Assert.assertEquals(2, groupedEvents.keys.size)
        Assert.assertEquals(2, groupedEvents.values.size)
        val firstEvent = groupedEvents.keys.toList()[0]
        val secondEvent = groupedEvents.keys.toList()[1]
        Assert.assertTrue(firstEvent.getDate()!! > secondEvent.getDate()!!)
        Assert.assertEquals(
            "GGD, RIVM",
            groupedEvents.values.first().map { it.providerIdentifier }.joinToString(", ")
        )
    }

    @Test
    fun `combine same events from same provider to 1 event`() {
        val remoteProtocol = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination(), vaccination())
        )

        val combinedEvents = util.groupEvents(listOf(remoteProtocol))

        Assert.assertEquals(1, combinedEvents.size)
    }

    @Test
    fun `combine same date events with different hpk and manufacturer from same provider to 2 events`() {
        val remoteProtocol = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination(
                hpkCode = "hpkCode2",
            ), vaccination(
                hpkCode = "hpkCode1",
            ))
        )

        val combinedEvents = util.groupEvents(listOf(remoteProtocol))

        Assert.assertEquals(2, combinedEvents.size)
    }

    @Test
    fun `combine different date events with same hpk and manufacturer from same provider to 2 events`() {
        val remoteProtocol = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(vaccination(
                clock = clock2
            ), vaccination(
                clock = clock1
            ))
        )

        val combinedEvents = util.groupEvents(listOf(remoteProtocol))

        Assert.assertEquals(2, combinedEvents.size)
    }

    @Test
    fun `events should be sorted by descending date regardless of type`() {
        val vaccination1 = vaccination(
            doseNumber = "1",
            totalDoses = "2",
            clock = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val vaccination2 = vaccination(
            doseNumber = "2",
            totalDoses = "2",
            clock = Clock.fixed(Instant.parse("2021-06-09T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val event1 = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(
                vaccination1,
                vaccination2
            )
        )

        val recovery = recovery(
            clock = Clock.fixed(Instant.parse("2021-06-20T00:00:00.00Z"), ZoneId.of("UTC"))
        )
        val event2 = RemoteProtocol3(
            providerIdentifier = "GGD",
            protocolVersion = "2",
            status = RemoteProtocol.Status.COMPLETE,
            holder = holder(),
            events = listOf(
                recovery
            )
        )

        val groupedEvents = util.groupEvents(listOf(event1, event2))

        assertEquals(recovery, groupedEvents.keys.first())
        assertEquals(vaccination2, groupedEvents.keys.elementAt(1))
        assertEquals(vaccination1, groupedEvents.keys.last())
    }

    private fun holder(): RemoteProtocol3.Holder {
        return RemoteProtocol3.Holder(
            infix = null,
            firstName = "First",
            lastName = "Last",
            birthDate = "01-08-1980",
        )
    }

    private fun vaccination(
        doseNumber: String = "1",
        totalDoses: String = "1",
        hpkCode: String? = "hpkCode",
        manufacturer: String? = null,
        clock: Clock = clock1,
    ) = RemoteEventVaccination(
        type = "vaccination",
        unique = null,
        vaccination = RemoteEventVaccination.Vaccination(
            date = LocalDate.now(clock),
            type = "vaccination",
            hpkCode = hpkCode,
            brand = "Brand",
            doseNumber = doseNumber,
            totalDoses = totalDoses,
            manufacturer = manufacturer,
            completedByMedicalStatement = null,
            completedByPersonalStatement = null,
            country = null,
            completionReason = null,
        )
    )

    private fun recovery(
        clock: Clock
    ) = RemoteEventRecovery(
        type = "recovery",
        unique = "",
        isSpecimen = true,
        recovery = RemoteEventRecovery.Recovery(
            sampleDate = LocalDate.now(clock),
            validFrom = LocalDate.now(clock),
            validUntil = LocalDate.now(clock)
        )
    )
}