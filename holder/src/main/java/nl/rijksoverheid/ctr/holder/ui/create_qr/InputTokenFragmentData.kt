package nl.rijksoverheid.ctr.holder.ui.create_qr

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
sealed class InputTokenFragmentData(
    @StringRes val description: Int,
    @StringRes val descriptionDeeplink: Int,
    @StringRes val uniqueCodeInputHeader: Int,
    @StringRes val uniqueCodeInputHint: Int,
    @StringRes val uniqueCodeInputHintScreenReader: Int,
    @StringRes val noUniqueCodeEntered: Int,
    @StringRes val noCodeText: Int,
    @StringRes val noCodeDialogTitle: Int,
    @StringRes val noCodeDialogDescription: Int,
    @StringRes val noResultScreenToolbarTitle: Int,
    @StringRes val noResultScreenTitle: Int,
    @StringRes val noResultScreenDescription: Int,
    @StringRes val buttonText: Int,
): Parcelable {
    @Parcelize
    object CommercialTest: InputTokenFragmentData(
        description = R.string.commercial_test_code_description,
        descriptionDeeplink = R.string.commercial_test_verification_code_description_deeplink,
        uniqueCodeInputHeader = R.string.commercial_test_unique_code_header,
        uniqueCodeInputHint = R.string.commercial_test_unique_code_hint,
        uniqueCodeInputHintScreenReader = R.string.commercial_test_unique_code_hint_screenreader,
        noUniqueCodeEntered = R.string.commercial_test_error_empty_retrieval_code,
        noCodeText = R.string.commercial_test_type_no_code_title,
        noCodeDialogTitle = R.string.commercial_test_type_no_code_title,
        noCodeDialogDescription = R.string.commercial_test_type_no_code_description,
        noResultScreenToolbarTitle = R.string.commercial_test_type_title,
        noResultScreenTitle = R.string.no_negative_test_result_title,
        noResultScreenDescription = R.string.no_negative_test_result_description,
        buttonText = R.string.commercial_test_button,
    )

    @Parcelize
    object VisitorPass: InputTokenFragmentData(
        description = R.string.visitorpass_code_description,
        descriptionDeeplink = R.string.visitorpass_code_description_deeplink,
        uniqueCodeInputHeader = R.string.visitorpass_code_review_input,
        uniqueCodeInputHint = R.string.visitorpass_code_review_placeholder,
        uniqueCodeInputHintScreenReader = R.string.visitorpass_code_review_placeholder_screenreader,
        noUniqueCodeEntered = R.string.visitorpass_token_error_empty_token,
        noCodeText = R.string.visitorpass_code_review_button,
        noCodeDialogTitle = R.string.visitorpass_token_modal_notoken_title,
        noCodeDialogDescription = R.string.visitorpass_token_modal_notoken_details,
        noResultScreenToolbarTitle = R.string.holder_event_vaccination_assessment_toolbar_title,
        noResultScreenTitle = R.string.holder_event_vaccination_assessment_nolist_title,
        noResultScreenDescription = R.string.holder_event_vaccination_assessment_nolist_message,
        buttonText = R.string.onboarding_next,
    )
}
