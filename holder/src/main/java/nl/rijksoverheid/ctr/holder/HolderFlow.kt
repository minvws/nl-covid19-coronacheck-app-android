package nl.rijksoverheid.ctr.holder

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.shared.models.Flow
import org.bouncycastle.asn1.x509.Holder

sealed class HolderFlow(code: Int) : Flow(code), Parcelable {

    @Parcelize
    object Startup: HolderFlow(0)

    @Parcelize
    object CommercialTest: HolderFlow(1)

    @Parcelize
    object Vaccination: HolderFlow(2)

    @Parcelize
    object Recovery: HolderFlow(3)

    @Parcelize
    object DigidTest: HolderFlow(4)

    @Parcelize
    object HkviScan: HolderFlow(5)

    @Parcelize
    object SyncGreenCards: HolderFlow(7)

    @Parcelize
    object PositiveTest: HolderFlow(8)

    @Parcelize
    object VaccinationAssessment: HolderFlow(9)

    @Parcelize
    object VaccinationBesIslands: HolderFlow(10)

    @Parcelize
    object VaccinationAndPositiveTest: HolderFlow(11)
}