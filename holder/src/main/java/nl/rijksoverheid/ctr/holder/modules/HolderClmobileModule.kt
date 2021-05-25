package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.shared.ClmobileWrapper
import nl.rijksoverheid.ctr.shared.ClmobileWrapperImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val holderClmobileModule = module {
    single<ClmobileWrapper> {
        ClmobileWrapperImpl()
    }
}
