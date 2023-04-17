/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.shared.models.Flow

sealed class HolderFlow(code: Int) : Flow(code), Parcelable {

    @Parcelize
    object Startup : HolderFlow(0)

    @Parcelize
    object CommercialTest : HolderFlow(1)

    @Parcelize
    object Vaccination : HolderFlow(2)

    @Parcelize
    object Recovery : HolderFlow(3)

    @Parcelize
    object DigidTest : HolderFlow(4)

    @Parcelize
    object HkviScan : HolderFlow(5)

    @Parcelize
    data class HkviScanned(val remoteProtocol: RemoteProtocol) : HolderFlow(5)

    @Parcelize
    object SyncGreenCards : HolderFlow(7)

    @Parcelize
    object VaccinationAndPositiveTest : HolderFlow(8)

    @Parcelize
    object VaccinationBesIslands : HolderFlow(10)

    @Parcelize
    object ClearEvents : HolderFlow(11)

    @Parcelize
    object Refresh : HolderFlow(12)

    @Parcelize
    object FuzzyMatching : HolderFlow(13)
}
