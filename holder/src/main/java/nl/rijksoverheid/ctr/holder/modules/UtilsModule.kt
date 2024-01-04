package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.utils.CountryUtil
import nl.rijksoverheid.ctr.holder.utils.CountryUtilImpl
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtil
import nl.rijksoverheid.ctr.holder.utils.LocalDateUtilImpl
import nl.rijksoverheid.ctr.holder.utils.StringUtil
import nl.rijksoverheid.ctr.holder.utils.StringUtilImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val utilsModule = module {
    factory<CountryUtil> { CountryUtilImpl() }
    factory<LocalDateUtil> { LocalDateUtilImpl(get(), get()) }
    factory<StringUtil> { StringUtilImpl(get()) }
}
