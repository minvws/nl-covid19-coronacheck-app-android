package nl.rijksoverheid.ctr.holder.ui.create_qr.visitor_pass

import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.HolderFlow
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.shared.models.Flow


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VisitorPassIntroductionFragment: BaseFragment(R.layout.fragment_visitor_pass_introduction) {
    override fun onButtonClickWithRetryAction() {

    }

    override fun onButtonClickWithRetryTitle(): Int {
        return super.onButtonClickWithRetryTitle()
    }

    override fun getFlow(): Flow {
        return HolderFlow.Startup
    }
}