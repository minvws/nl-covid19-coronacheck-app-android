package nl.rijksoverheid.ctr.holder.saved_events.usecases

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.your_events.utils.EventGroupEntityUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.InfoScreenUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.entities.EventGroupEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Test

class GetSavedEventsUseCaseImplTest {
    private val holderDatabase: HolderDatabase = mockk()
    private val remoteEventUtil: RemoteEventUtil = mockk()
    private val eventGroupEntityUtil: EventGroupEntityUtil = mockk()
    private val getRemoteProtocolFromEventGroupUseCase: GetRemoteProtocolFromEventGroupUseCase = mockk()
    private val infoScreenUtil: InfoScreenUtil = mockk(relaxed = true)
    private val yourEventsFragmentUtil: YourEventsFragmentUtil = mockk()

    private val vaccination = EventGroupEntity(
        id = 1,
        walletId = 1,
        providerIdentifier = "ZZZ_dewd3d33",
        type = OriginType.Vaccination,
        scope = "",
        expiryDate = null,
        draft = false,
        jsonData = "".toByteArray()
    )

    private val recovery = EventGroupEntity(
        id = 2,
        walletId = 1,
        providerIdentifier = "ZZZ_dewd3d33",
        type = OriginType.Recovery,
        scope = "",
        expiryDate = null,
        draft = false,
        jsonData = "".toByteArray()
    )

    @Test
    fun `getSavedEvents return correct list of SavedEvents`() = runTest {
        val vaccinationEvent = mockk<RemoteEventVaccination>()
        val vaccinationProtocol = mockk<RemoteProtocol>().apply {
            coEvery { events } returns listOf(vaccinationEvent)
            coEvery { holder } returns mockk()
        }
        val recoveryEvent = mockk<RemoteEventRecovery>().apply {
            coEvery { recovery?.sampleDate } returns null
        }
        val recoveryProtocol = mockk<RemoteProtocol>().apply {
            coEvery { events } returns listOf(recoveryEvent)
            coEvery { holder } returns mockk()
        }
        coEvery { holderDatabase.eventGroupDao().getAll() } returns listOf(vaccination, recovery)
        coEvery { remoteEventUtil.isDccEvent(any()) } returns false
        coEvery { getRemoteProtocolFromEventGroupUseCase.get(vaccination) } returns vaccinationProtocol
        coEvery { getRemoteProtocolFromEventGroupUseCase.get(recovery) } returns recoveryProtocol
        coEvery { yourEventsFragmentUtil.getFullName(any()) } returns "Onoma Epitheto"
        coEvery { yourEventsFragmentUtil.getBirthDate(any()) } returns "01-08-1990"
        coEvery { eventGroupEntityUtil.getProviderName(any()) } returns "MVWS-TEST"
        val getSavedEventsUseCase = GetSavedEventsUseCaseImpl(mockk(), holderDatabase, remoteEventUtil, eventGroupEntityUtil, getRemoteProtocolFromEventGroupUseCase, infoScreenUtil, yourEventsFragmentUtil)

        val savedEvents = getSavedEventsUseCase.getSavedEvents()

        assertEquals(vaccination, savedEvents[1].eventGroupEntity)
        assertEquals(recovery, savedEvents[0].eventGroupEntity)
        verify {
            infoScreenUtil.getForVaccination(vaccinationEvent, "Onoma Epitheto", "01-08-1990", "MVWS-TEST", null, false)
        }
        verify {
            infoScreenUtil.getForRecovery(recoveryEvent, "", "Onoma Epitheto", "01-08-1990", null, false)
        }
    }
}
