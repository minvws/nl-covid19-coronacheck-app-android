package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtilImpl
import nl.rijksoverheid.ctr.holder.api.TestProviderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.TestProviderApiClientUtilImpl
import nl.rijksoverheid.ctr.holder.api.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepository
import nl.rijksoverheid.ctr.holder.api.repositories.CoronaCheckRepositoryImpl
import nl.rijksoverheid.ctr.holder.api.repositories.DigidAuthenticationRepository
import nl.rijksoverheid.ctr.holder.api.repositories.EventProviderRepository
import nl.rijksoverheid.ctr.holder.api.repositories.EventProviderRepositoryImpl
import nl.rijksoverheid.ctr.holder.api.repositories.MijnCNAuthenticationRepository
import nl.rijksoverheid.ctr.holder.api.repositories.TestProviderRepository
import nl.rijksoverheid.ctr.holder.api.repositories.TestProviderRepositoryImpl
import nl.rijksoverheid.ctr.holder.modules.qualifier.ErrorResponseQualifier
import nl.rijksoverheid.ctr.holder.modules.qualifier.LoginQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val repositoriesModule = module {
    single<AuthenticationRepository>(named(LoginQualifier.DIGID)) { DigidAuthenticationRepository() }
    single<AuthenticationRepository>(named(LoginQualifier.MIJN_CN)) {
        MijnCNAuthenticationRepository(
            get(),
            get()
        )
    }
    factory<HolderApiClientUtil> {
        HolderApiClientUtilImpl(get(), get())
    }
    factory<CoronaCheckRepository> {
        CoronaCheckRepositoryImpl(
            get(),
            get(),
            get(),
            get(named(ErrorResponseQualifier.CORONA_CHECK)),
            get(),
            get()
        )
    }
    factory<TestProviderRepository> {
        TestProviderRepositoryImpl(
            get(),
            get(),
            get(named("SignedResponseWithModel"))
        )
    }
    factory<TestProviderApiClientUtil> {
        TestProviderApiClientUtilImpl(
            get(),
            get(),
            get()
        )
    }
    factory<EventProviderRepository> {
        EventProviderRepositoryImpl(
            get(),
            get()
        )
    }
}
