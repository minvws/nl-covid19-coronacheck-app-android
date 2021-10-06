package nl.rijksoverheid.ctr.holder

import android.os.Parcelable
import nl.rijksoverheid.ctr.shared.models.Flow

sealed class HolderFlow(code: Int) : Flow(code) {
    object Startup: HolderFlow(0), Parcelable
    object CommercialTest: HolderFlow(1)
    object Vaccination: HolderFlow(2)
    object Recovery: HolderFlow(3)
    object DigidTest: HolderFlow(4)
    object HkviScan: HolderFlow(5)
    object SyncGreenCards: HolderFlow(7)
}