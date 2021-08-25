package nl.rijksoverheid.ctr.shared.models

import java.io.Serializable

/**
 * Represents a particular step the user has triggered
 */
open class Step(open val code: Int) : Serializable