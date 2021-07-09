package nl.rijksoverheid.ctr.holder.ui.myoverview.usecases

import android.content.Intent

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface ReturnToAppUseCase {
    fun get(uri: String): Intent?
}

class ReturnToAppUseCaseImpl() : ReturnToAppUseCase {

    override fun get(uri: String): Intent? {
        return Intent()
    }


}
