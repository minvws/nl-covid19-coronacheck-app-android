/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteProtocol3
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult2
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.models.ErrorResult
import nl.rijksoverheid.ctr.shared.models.Flow

class CommercialTestInputTokenFragment: InputTokenFragment() {

    private val args: CommercialTestInputTokenFragmentArgs by navArgs()

    override fun getFlow(): Flow {
        return HolderFlow.CommercialTest
    }

    override fun getFragmentData(): InputTokenFragmentData {
        return InputTokenFragmentData.CommercialTest
    }

    override fun navigateCouldNotCreateQr() {
        CommercialTestInputTokenFragmentDirections.actionCouldNotCreateQr(
            toolbarTitle = getString(getFragmentData().noResultScreenToolbarTitle),
            title = getString(getFragmentData().noResultScreenTitle),
            description = getString(getFragmentData().noResultScreenDescription),
            buttonTitle = getString(R.string.back_to_overview)
        )
    }

    override fun navigateMyEvents(result: TestResult.NegativeTestResult) {
        when (result.remoteTestResult) {
            is RemoteTestResult2 -> {
                findNavController().navigate(
                    CommercialTestInputTokenFragmentDirections.actionYourEvents(
                        type = YourEventsFragmentType.TestResult2(
                            remoteTestResult = result.remoteTestResult,
                            rawResponse = result.signedResponseWithTestResult.rawResponse
                        ),
                        toolbarTitle = getString(R.string.your_negative_test_results_toolbar),
                        flow = HolderFlow.CommercialTest
                    )
                )
            }
            is RemoteProtocol3 -> {
                findNavController().navigate(
                    CommercialTestInputTokenFragmentDirections.actionYourEvents(
                        type = YourEventsFragmentType.RemoteProtocol3Type(
                            mapOf(result.remoteTestResult to result.signedResponseWithTestResult.rawResponse),
                            originType = getOriginType(result.remoteTestResult),
                        ),
                        flow = HolderFlow.CommercialTest,
                        toolbarTitle = getString(getYourEventsToolbarTitle(result.remoteTestResult)),
                    )
                )
            }
        }
    }

    override fun getDeeplinkToken(): String? {
        return args.token
    }

    override fun presentInputTokenError(errorResult: ErrorResult) {
        presentError(errorResult)
    }
}