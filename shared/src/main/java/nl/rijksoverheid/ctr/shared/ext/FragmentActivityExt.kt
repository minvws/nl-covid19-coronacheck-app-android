package nl.rijksoverheid.ctr.shared.ext

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun FragmentActivity.findNavControllerSafety(currentId: Int): NavController? {
    return try {
        findNavController(currentId)
    } catch (e: Exception) {
        null
    }
}
