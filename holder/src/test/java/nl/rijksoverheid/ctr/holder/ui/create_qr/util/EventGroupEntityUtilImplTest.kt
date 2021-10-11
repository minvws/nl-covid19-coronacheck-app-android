package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import kotlinx.coroutines.runBlocking
import nl.rijksoverheid.ctr.holder.fakeRemoteEventUtil
import nl.rijksoverheid.ctr.holder.fakeRemoteEventVaccination
import nl.rijksoverheid.ctr.holder.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventVaccination
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.OffsetDateTime

class EventGroupEntityUtilImplTest {

    @Test
    fun `allVaccinationEvents returns empty list if no vaccinations`() = runBlocking {
        val util = EventGroupEntityUtilImpl(
            remoteEventUtil = fakeRemoteEventUtil()
        )

        val amount = util.amountOfVaccinationEvents(
            eventGroupEntities = listOf()
        )

        assertEquals(0, amount)
    }

    @Test
    fun `allVaccinationEvents returns 4 events if 2 ZZZ vaccination event groups with 2 events each`() = runBlocking {
        val util = EventGroupEntityUtilImpl(
            remoteEventUtil = fakeRemoteEventUtil(
                getRemoteEventsFromNonDcc = listOf(
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination
                    ),
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination
                    )
                )
            )
        )

        val amount = util.amountOfVaccinationEvents(
            eventGroupEntities = listOf(
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Vaccination,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                ),
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Vaccination,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                ),
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Test,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                )
            )
        )

        assertEquals(4, amount)
    }

    @Test
    fun `allVaccinationEvents returns 5 events if 2 ZZZ vaccination event groups with 2 events each and 1 DCC event`() = runBlocking {
        val util = EventGroupEntityUtilImpl(
            remoteEventUtil = fakeRemoteEventUtil(
                getRemoteEventsFromNonDcc = listOf(
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination
                    ),
                    RemoteEventVaccination(
                        type = "",
                        unique = "",
                        vaccination = fakeRemoteEventVaccination
                    )
                )
            )
        )

        val amount = util.amountOfVaccinationEvents(
            eventGroupEntities = listOf(
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Vaccination,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                ),
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Vaccination,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                ),
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "dcc",
                    type = OriginType.Vaccination,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                ),
                EventGroupEntity(
                    id = 1,
                    walletId = 1,
                    providerIdentifier = "ZZZ",
                    type = OriginType.Test,
                    maxIssuedAt = OffsetDateTime.now(),
                    jsonData = "".toByteArray()
                )
            )
        )

        assertEquals(5, amount)
    }
}