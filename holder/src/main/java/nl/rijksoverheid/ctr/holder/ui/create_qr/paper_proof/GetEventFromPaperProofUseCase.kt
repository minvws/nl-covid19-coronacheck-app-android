package nl.rijksoverheid.ctr.holder.ui.create_qr.paper_proof

import android.app.Application
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.shared.MobileCoreWrapper
import nl.rijksoverheid.ctr.shared.ext.getStringOrNull
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface GetEventFromQrUseCase {

    fun get(qrCode: String)
}

class GetEventFromQrUseCaseImpl(
    private val mobileCoreWrapper: MobileCoreWrapper,
    private val application: Application,
    private val cachedAppConfigUseCase: CachedAppConfigUseCase
) : GetEventFromQrUseCase {

    override fun get(qrCode: String) {
        val credentials = mobileCoreWrapper.readEuropeanCredential(qrCode.toByteArray())
        val dcc = credentials.optJSONObject("dcc")
        val test = dcc.getJSONArray("t").optJSONObject(0)


        val fullName = "${dcc.optJSONObject("nam").getStringOrNull("fn")}, ${
            dcc.optJSONObject("nam").getStringOrNull("gn")
        }"

        val birthDate = dcc.getStringOrNull("dob")?.let { birthDate ->
            try {
                LocalDate.parse(birthDate, DateTimeFormatter.ISO_DATE).formatDayMonthYear()
            } catch (e: Exception) {
                ""
            }
        } ?: ""


        val testType = cachedAppConfigUseCase.getCachedAppConfig().euTestTypes.firstOrNull {
            it.code == test.getStringOrNull("tt")
        }?.name ?: test.getStringOrNull("tt") ?: ""

        val testName = test.getStringOrNull("nm") ?: ""

        val testDate = test.getStringOrNull("sc")?.let {
            try {
                OffsetDateTime.parse(it, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .formatDateTime(application)
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        val testLocation = test.getStringOrNull("tc") ?: ""

        val manufacturer =
            cachedAppConfigUseCase.getCachedAppConfig().euManufacturers.firstOrNull {
                it.code == test.getStringOrNull("ma")
            }?.name ?: test.getStringOrNull("ma") ?: ""

        val vaccinationCountry = test.getStringOrNull("co")
        val uniqueCode = test.getStringOrNull("ci")
    }
}