package nl.rijksoverheid.ctr.shared.error

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class Flow(open val code: Int) : Parcelable