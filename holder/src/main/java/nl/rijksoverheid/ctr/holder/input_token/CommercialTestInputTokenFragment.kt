/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.input_token

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.input_token.usecases.TestResult
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.your_events.YourEventsFragmentType
import nl.rijksoverheid.ctr.shared.models.Flow

class CommercialTestInputTokenFragment : InputTokenFragment() {

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
        findNavController().navigate(
            CommercialTestInputTokenFragmentDirections.actionYourEvents(
                type = YourEventsFragmentType.RemoteProtocol3Type(
                    remoteEvents = mapOf(result.remoteTestResult to result.signedResponseWithTestResult.rawResponse)
                ),
                flow = HolderFlow.CommercialTest,
                toolbarTitle = getString(R.string.your_negative_test_results_toolbar)
            )
        )
    }

    override fun getDeeplinkToken(): String? {
        return args.token
    }
}
