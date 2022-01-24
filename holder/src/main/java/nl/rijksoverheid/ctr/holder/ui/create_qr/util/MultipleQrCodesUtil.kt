/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr.util

import nl.rijksoverheid.ctr.holder.ui.myoverview.models.QrCodeData

interface MultipleQrCodesUtil {
    fun getMostRelevantQrCodeIndex(vaccinations: List<QrCodeData.European.Vaccination>): Int
}

class MultipleQrCodesUtilImpl : MultipleQrCodesUtil {

    override fun getMostRelevantQrCodeIndex(vaccinations: List<QrCodeData.European.Vaccination>): Int {
        val mostRelevantVaccination = vaccinations.sortedWith(
            compareBy<QrCodeData.European.Vaccination> { !it.isHidden }
                .thenBy { it.dose }
                .thenBy { it.ofTotalDoses }
        ).last()
        return vaccinations.indexOfFirst { it == mostRelevantVaccination }
    }
}