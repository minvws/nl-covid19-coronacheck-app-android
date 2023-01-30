/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.your_events.utils

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.appconfig.api.model.AppConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.holder.your_events.utils.RemoteEventUtil
import nl.rijksoverheid.ctr.holder.your_events.utils.YourEventsFragmentUtilImpl
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YourEventsFragmentUtilImplTest : AutoCloseKoinTest() {

    private val eventProviders = listOf(
        AppConfig.Code(name = "MVWS-TEST", code = "ZZZ"), AppConfig.Code(name = "Test Provider", code = "test")
    )

    @Test
    fun `getNoOriginTypeCopy returns correct copy for DCC`() {
        val util = YourEventsFragmentUtilImpl(mockk())

        val dcc = mockk<YourEventsFragmentType.DCC>()
        val copy = util.getNoOriginTypeCopy(dcc, HolderFlow.Startup)

        assertEquals(R.string.rule_engine_no_test_origin_description_scanned_qr_code, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for RemoteProtocol3Type with origin test`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Test
        val test = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { test.remoteEvents } returns getRemoteProtocol3(remoteEvent)
        val copy = util.getNoOriginTypeCopy(test, HolderFlow.Startup)

        assertEquals(R.string.rule_engine_no_test_origin_description_negative_test, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for RemoteProtocol3Type with origin vaccination`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Vaccination
        val vaccination = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { vaccination.remoteEvents } returns getRemoteProtocol3(remoteEvent)
        val copy = util.getNoOriginTypeCopy(vaccination, HolderFlow.Startup)

        assertEquals(R.string.rule_engine_no_test_origin_description_vaccination, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for RemoteProtocol3Type with origin vaccination in positive test flow`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Vaccination
        val vaccination = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { vaccination.remoteEvents } returns getRemoteProtocol3(remoteEvent)
        val copy = util.getNoOriginTypeCopy(vaccination, HolderFlow.VaccinationAndPositiveTest)

        assertEquals(R.string.general_retrievedDetails, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for RemoteProtocol3Type with origin recovery`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Recovery
        val type = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { type.remoteEvents } returns getRemoteProtocol3(remoteEvent)
        val copy = util.getNoOriginTypeCopy(type, HolderFlow.Startup)

        assertEquals(R.string.rule_engine_no_test_origin_description_positive_test, copy)
    }

    @Test
    fun `getProviderName returns readable provider name if mapping is available`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val providerName = util.getProviderName(
            providers = eventProviders,
            providerIdentifier = "test"
        )

        assertEquals("Test Provider", providerName)
    }

    @Test
    fun `getProviderName returns provider identifier if mapping is not available`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val providerName = util.getProviderName(
            providers = eventProviders,
            providerIdentifier = "test2"
        )

        assertEquals("test2", providerName)
    }

    @Test
    fun `getCancelDialogDescription returns correct copy for DCC`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val dcc = mockk<YourEventsFragmentType.DCC>()
        val copy = util.getCancelDialogDescription(
            type = dcc
        )

        assertEquals(copy, R.string.holder_dcc_alert_message)
    }

    @Test
    fun `getCancelDialogDescription returns correct copy for RemoteProtocol3Type with origin recovery`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Recovery
        val type = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { type.remoteEvents } returns getRemoteProtocol3(remoteEvent)

        val copy = util.getCancelDialogDescription(
            type = type
        )

        assertEquals(copy, R.string.holder_recovery_alert_message)
    }

    @Test
    fun `getCancelDialogDescription returns correct copy for RemoteProtocol3Type with origin vaccination`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Vaccination
        val type = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { type.remoteEvents } returns getRemoteProtocol3(remoteEvent)

        val copy = util.getCancelDialogDescription(
            type = type
        )

        assertEquals(copy, R.string.holder_vaccination_alert_message)
    }

    @Test
    fun `getCancelDialogDescription returns correct copy for RemoteProtocol3Type with origin vaccination assessment`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.VaccinationAssessment
        val type = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { type.remoteEvents } returns getRemoteProtocol3(remoteEvent)

        val copy = util.getCancelDialogDescription(
            type = type
        )

        assertEquals(copy, R.string.holder_event_vaccination_assessment_alert_message)
    }

    @Test
    fun `getFullName returns correct concatted name when no infix`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val holder = RemoteProtocol.Holder(
            infix = "",
            firstName = "Bob",
            lastName = "Bouwer",
            birthDate = ""
        )

        val fullName = util.getFullName(
            holder = holder
        )

        assertEquals("Bouwer, Bob", fullName)
    }

    @Test
    fun `getFullName returns correct concatted name when infix`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val holder = RemoteProtocol.Holder(
            infix = "de",
            firstName = "Bob",
            lastName = "Bouwer",
            birthDate = ""
        )

        val fullName = util.getFullName(
            holder = holder
        )

        assertEquals("de Bouwer, Bob", fullName)
    }

    @Test
    fun `getBirthDate returns formatted birthday if parseable`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val holder = RemoteProtocol.Holder(
            infix = "de",
            firstName = "Bob",
            lastName = "Bouwer",
            birthDate = "1970-01-01"
        )

        val birthDate = util.getBirthDate(
            holder = holder
        )

        assertEquals("1 January 1970", birthDate)
    }

    @Test
    fun `getBirthDate returns XX if birthday not known`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val holder = RemoteProtocol.Holder(
            infix = "de",
            firstName = "Bob",
            lastName = "Bouwer",
            birthDate = "XX"
        )

        val birthDate = util.getBirthDate(
            holder = holder
        )

        assertEquals("XX", birthDate)
    }

    @Test
    fun `getHeaderCopy returns correct copy when type dcc`() {
        val util = YourEventsFragmentUtilImpl(mockk())

        val copy = util.getHeaderCopy(
            type = YourEventsFragmentType.DCC(mockk(), "".toByteArray(), mockk())
        )

        assertEquals(R.string.holder_listRemoteEvents_paperflow_message, copy)
    }

    @Test
    fun `getHeaderCopy returns correct copy when type is RemoteProtocol3Type recovery`() {
        val util = YourEventsFragmentUtilImpl(mockk<RemoteEventUtil>().apply {
            every { getOriginType(any()) } returns OriginType.Recovery
        })

        val copy = util.getHeaderCopy(
            type = YourEventsFragmentType.RemoteProtocol3Type(getRemoteProtocol3(mockk<RemoteEvent>().apply {
                every { type } returns "recovery"
            }), listOf())
        )

        assertEquals(R.string.holder_listRemoteEvents_recovery_message, copy)
    }

    @Test
    fun `getHeaderCopy returns correct copy when type is RemoteProtocol3Type vaccinationassessment`() {
        val util = YourEventsFragmentUtilImpl(mockk<RemoteEventUtil>().apply {
            every { getOriginType(any()) } returns OriginType.VaccinationAssessment
        })

        val copy = util.getHeaderCopy(
            type = YourEventsFragmentType.RemoteProtocol3Type(getRemoteProtocol3(mockk<RemoteEvent>().apply {
                every { type } returns "vaccinationassessment"
            }), listOf())
        )

        assertEquals(R.string.holder_listRemoteEvents_vaccinationAssessment_message, copy)
    }

    private fun getRemoteProtocol3(event: RemoteEvent) =
        mapOf(
            RemoteProtocol(
                "",
                "",
                RemoteProtocol.Status.UNKNOWN,
                null,
                listOf(event)
            ) to ByteArray(1)
        )
}
