/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared

import java.util.concurrent.TimeUnit

object AccessibilityConstants {
    val ACCESSIBILITY_FOCUS_DELAY = TimeUnit.MILLISECONDS.toMillis(500)
    val ACCESSIBILITY_DELEGATE_DELAY = TimeUnit.MILLISECONDS.toMillis(100)
}