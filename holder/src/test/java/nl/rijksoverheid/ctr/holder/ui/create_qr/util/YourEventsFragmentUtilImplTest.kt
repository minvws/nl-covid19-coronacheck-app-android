/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.ui.create_qr.YourEventsFragmentType
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.EventProvider
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YourEventsFragmentUtilImplTest : AutoCloseKoinTest() {

    @Test
    fun `getNoOriginTypeCopy returns correct copy for TestResult2`() {
        val util = YourEventsFragmentUtilImpl(mockk())

        val testResult2 = mockk<YourEventsFragmentType.TestResult2>()
        val copy = util.getNoOriginTypeCopy(testResult2)

        assertEquals(R.string.rule_engine_no_test_origin_description_negative_test, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for DCC`() {
        val util = YourEventsFragmentUtilImpl(mockk())

        val dcc = mockk<YourEventsFragmentType.DCC>()
        val copy = util.getNoOriginTypeCopy(dcc)

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
        val copy = util.getNoOriginTypeCopy(test)

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
        val copy = util.getNoOriginTypeCopy(vaccination)

        assertEquals(R.string.rule_engine_no_test_origin_description_vaccination, copy)
    }

    @Test
    fun `getNoOriginTypeCopy returns correct copy for RemoteProtocol3Type with origin recovery`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val remoteEvent = RemoteEventNegativeTest(null, null, null, null)
        every { remoteEventUtil.getOriginType(remoteEvent) } returns OriginType.Recovery
        val type = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        every { type.remoteEvents } returns getRemoteProtocol3(remoteEvent)
        val copy = util.getNoOriginTypeCopy(type)

        assertEquals(R.string.rule_engine_no_test_origin_description_positive_test, copy)
    }

    @Test
    fun `getProviderName returns readable provider name if mapping is available`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val vaccination = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        val eventProvider = EventProvider(
            identifier = "test",
            name = "Test Provider"
        )
        every { vaccination.eventProviders } answers { listOf(eventProvider) }

        val providerName = util.getProviderName(
            type = vaccination,
            providerIdentifier = "test"
        )

        assertEquals("Test Provider", providerName)
    }

    @Test
    fun `getProviderName returns provider identifier if mapping is not available`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val vaccination = mockk<YourEventsFragmentType.RemoteProtocol3Type>()
        val eventProvider = EventProvider(
            identifier = "test2",
            name = "Test Provider"
        )
        every { vaccination.eventProviders } answers { listOf(eventProvider) }

        val providerName = util.getProviderName(
            type = vaccination,
            providerIdentifier = "test"
        )

        assertEquals("test", providerName)
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
    fun `getCancelDialogDescription returns correct copy for RemoteProtocol3Type with origin test`() {
        val remoteEventUtil: RemoteEventUtil = mockk()
        val util = YourEventsFragmentUtilImpl(remoteEventUtil)

        val testResult2 = mockk<YourEventsFragmentType.TestResult2>()
        val copy = util.getCancelDialogDescription(
            type = testResult2
        )

        assertEquals(copy, R.string.holder_test_alert_message)
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

        val holder = RemoteProtocol3.Holder(
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

        val holder = RemoteProtocol3.Holder(
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

        val holder = RemoteProtocol3.Holder(
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

        val holder = RemoteProtocol3.Holder(
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

    private fun getRemoteProtocol3(element: RemoteEventNegativeTest) =
        mapOf(
            RemoteProtocol3(
                "",
                "",
                RemoteProtocol.Status.UNKNOWN,
                null,
                listOf(element)
            ) to ByteArray(1)
        )
}