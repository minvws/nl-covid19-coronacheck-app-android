package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.verifier.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class PaperScanResultFragmentData(
    @StringRes val header: Int,
    @StringRes val description: Int,
    @StringRes val buttonText: Int,
    @StringRes val secondaryButtonText: Int,
): Parcelable {
    @Parcelize
    object ScanTest: PaperScanResultFragmentData(
        header = R.string.verifier_scannextinstruction_title_test,
        description = R.string.verifier_scannextinstruction_header_test,
        buttonText = R.string.verifier_scannextinstruction_button_scan_next_test,
        secondaryButtonText = R.string.verifier_scannextinstruction_button_deny_access_test,
    )

    @Parcelize
    object ScanVaccinationOrRecovery: PaperScanResultFragmentData(
        header = R.string.verifier_scannextinstruction_title_supplemental,
        description = R.string.verifier_scannextinstruction_header_supplemental,
        buttonText = R.string.verifier_scannextinstruction_button_scan_next_supplemental,
        secondaryButtonText = R.string.verifier_scannextinstruction_button_deny_access_supplemental,
    )
}
