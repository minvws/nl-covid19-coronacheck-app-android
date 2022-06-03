package nl.rijksoverheid.ctr.introduction.status.usecases

import nl.rijksoverheid.ctr.introduction.status.models.IntroductionData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC    LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface IntroductionStatusUseCase {
    fun getIntroductionRequired(): Boolean
    fun getData(): IntroductionData
}
