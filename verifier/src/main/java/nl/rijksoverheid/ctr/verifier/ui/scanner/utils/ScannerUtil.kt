package nl.rijksoverheid.ctr.verifier.ui.scanner.utils

import android.app.Activity
import androidx.navigation.Navigation
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.RootNavDirections

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface ScannerUtil {
    fun launchScanner(activity: Activity)
}

class ScannerUtilImpl : ScannerUtil {

    override fun launchScanner(activity: Activity) {
        Navigation.findNavController(activity, R.id.main_nav_host_fragment)
            .navigate(RootNavDirections.actionScanner())
    }
}
