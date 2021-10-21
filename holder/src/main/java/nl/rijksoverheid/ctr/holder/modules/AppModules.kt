package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCaseImpl
import nl.rijksoverheid.ctr.holder.persistence.*
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManager
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManagerImpl
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCase
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCaseImpl
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCaseImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val appModule = module {
    factory<DeviceRootedUseCase> { DeviceRootedUseCaseImpl(androidContext()) }
    factory<DeviceSecureUseCase> { DeviceSecureUseCaseImpl(androidContext()) }
    factory<CachedAppConfigUseCase> {
        CachedAppConfigUseCaseImpl(
            get(),
            androidContext().filesDir.path,
            get()
        )
    }

    factory<TestResultsMigrationManager> { TestResultsMigrationManagerImpl(get()) }

    factory<WorkerManagerWrapper> { WorkerManagerWrapperImpl(androidContext(), get()) }

    factory<ReturnToExternalAppUseCase> {
        ReturnToExternalAppUseCaseImpl(get())
    }

    factory {
        HolderWorkerFactory(get(), get())
    }
}
