/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.NewDisclosurePolicySeenUseCase
import nl.rijksoverheid.ctr.holder.ui.myoverview.usecases.NewDisclosurePolicySeenUseCaseImpl
import org.koin.dsl.module

val disclosurePolicyModule = module {
    factory<NewDisclosurePolicySeenUseCase> { NewDisclosurePolicySeenUseCaseImpl(get(), get()) }
}