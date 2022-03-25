package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtilImpl
import nl.rijksoverheid.ctr.holder.api.repositories.*
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
            get()
        )
    }
    factory<TestProviderRepository> {
        TestProviderRepositoryImpl(
            get(),
            get(),
            get(named("SignedResponseWithModel")),
        )
    }
    factory<EventProviderRepository> {
        EventProviderRepositoryImpl(
            get(),
            get(),
        )
    }
}
