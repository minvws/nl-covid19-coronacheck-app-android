package nl.rijksoverheid.ctr.design

import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.DialogUtilImpl
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtilImpl
import nl.rijksoverheid.ctr.design.utils.IntentUtil
import nl.rijksoverheid.ctr.design.utils.IntentUtilImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val designModule = module {
    factory<DialogUtil> { DialogUtilImpl() }
    factory<InfoFragmentUtil> { InfoFragmentUtilImpl() }
    factory<IntentUtil> { IntentUtilImpl(get()) }
}
