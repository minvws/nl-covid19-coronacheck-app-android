package nl.rijksoverheid.ctr.holder.fuzzy_matching

import nl.rijksoverheid.ctr.holder.dashboard.util.GreenCardUtil
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class FuzzyMatchingOnboardingViewModel(
    holderDatabase: HolderDatabase,
    greenCardUtil: GreenCardUtil
) : FuzzyMatchingBaseViewModel(holderDatabase, greenCardUtil)
