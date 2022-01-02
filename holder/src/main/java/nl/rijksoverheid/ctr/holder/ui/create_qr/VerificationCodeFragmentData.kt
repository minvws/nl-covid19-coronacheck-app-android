package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.view.View
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import nl.rijksoverheid.ctr.holder.R

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
sealed class VerificationCodeFragmentData(
    @StringRes val description: Int,
    @StringRes val uniqueCodeInputHint: Int,
    @StringRes val verificationCodeInputHint: Int,
    @StringRes val verificationCodeInputHelperText: Int,
    @StringRes val verificationCodeTextHint: Int,
    @StringRes val buttonText: Int,
): Parcelable {
    @Parcelize
    object CommercialTest: VerificationCodeFragmentData(
        description = R.string.commercial_test_code_description,
        uniqueCodeInputHint = R.string.commercial_test_unique_code_header,
        verificationCodeInputHint = R.string.commercial_test_verification_code_header,
        verificationCodeInputHelperText = R.string.commercial_test_verification_code_helper_text,
        verificationCodeTextHint = R.string.commercial_test_verification_code_hint,
        buttonText = R.string.commercial_test_button,
    )

    @Parcelize
    object VisitorPass: VerificationCodeFragmentData(
        description = R.string.visitorpass_code_description,
        uniqueCodeInputHint = R.string.visitorpass_code_input,
        verificationCodeInputHint = R.string.visitorpass_code_verification_input,
        verificationCodeInputHelperText = R.string.visitorpass_code_verification_input_explanation,
        verificationCodeTextHint = View.NO_ID,
        buttonText = R.string.onboarding_next,
    )
}
