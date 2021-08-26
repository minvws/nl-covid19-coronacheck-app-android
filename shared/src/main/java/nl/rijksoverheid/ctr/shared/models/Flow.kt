package nl.rijksoverheid.ctr.shared.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a particular flow you are currently in
 * Fragments should typically use this to indicate what the current flow is
 */
@Parcelize
open class Flow(open val code: Int) : Parcelable