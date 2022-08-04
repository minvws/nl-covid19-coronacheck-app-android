/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.usecases

import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase

class BuildConfigUseCaseImpl : BuildConfigUseCase {
    override fun getVersionCode(): Int {
        return BuildConfig.VERSION_CODE
    }
}
