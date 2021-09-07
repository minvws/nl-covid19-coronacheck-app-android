package nl.rijksoverheid.ctr.holder

import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.shared.factories.ErrorCodeStringFactory
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.models.*
import org.koin.android.ext.android.inject

/**
 * BaseFragment that should be used by all fragments that require error handling
 */
abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    protected val errorCodeStringFactory: ErrorCodeStringFactory by inject()
    private val dialogUtil: DialogUtil by inject()

    /**
     * Function that is also called when a network requests fails and a user presses the "retry" button
     */
    abstract fun onButtonClickWithRetryAction()

    /**
     * Get the current [Flow] for this screen
     */
    abstract fun getFlow(): Flow

    fun presentError(errorResult: ErrorResult, customerErrorDescription: String? = null) {
        if (errorResult is NetworkRequestResult.Failed.NetworkError) {
            dialogUtil.presentDialog(
                context = requireContext(),
                title = R.string.dialog_no_internet_connection_title,
                message = getString(R.string.dialog_no_internet_connection_description),
                positiveButtonText = R.string.dialog_retry,
                positiveButtonCallback = {
                    onButtonClickWithRetryAction()
                },
                negativeButtonText = R.string.dialog_close
            )
        } else {
            if (is429HttpError(errorResult) || errorResult is OpenIdErrorResult.ServerBusy ) {
                // On HTTP 429 or server busy error we make an exception and show a too busy screen
                presentError(
                    data = ErrorResultFragmentData(
                        title = getString(R.string.error_too_busy_title),
                        description = getString(R.string.error_too_busy_description),
                        buttonTitle = getString(R.string.back_to_overview),
                        buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                    )
                )
            } else {
                val errorCodeString = errorCodeStringFactory.get(
                    flow = getFlow(),
                    errorResults = listOf(errorResult)
                )

                val errorDescription = customerErrorDescription
                    ?: if (errorResult is NetworkRequestResult.Failed.CoronaCheckHttpError) {
                        getString(
                            R.string.error_something_went_wrong_http_error_description,
                            errorCodeString
                        )
                    } else {
                        getString(
                            R.string.error_something_went_wrong_making_proof_description,
                            errorCodeString
                        )
                    }

                val data = ErrorResultFragmentData(
                    title = getString(R.string.error_something_went_wrong_title),
                    description = errorDescription,
                    urlData = ErrorResultFragmentData.UrlData(
                        urlButtonTitle = getString(R.string.error_something_went_wrong_outage_button),
                        urlButtonUrl = getString(R.string.error_something_went_wrong_outage_button_url)
                    ),
                    buttonTitle = getString(R.string.back_to_overview),
                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                )
                presentError(data)
            }
        }
    }

    private fun is429HttpError(errorResult: ErrorResult) =
        errorResult is NetworkRequestResult.Failed.CoronaCheckHttpError && errorResult.e.code() == 429

    fun presentError(data: ErrorResultFragmentData) {
        findNavControllerSafety()?.navigate(R.id.action_error_result, ErrorResultFragment.getBundle(data))
    }
}