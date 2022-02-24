package nl.rijksoverheid.ctr.holder.modules

import android.content.Context
import nl.rijksoverheid.ctr.appconfig.usecases.*
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManager
import nl.rijksoverheid.ctr.holder.persistence.database.migration.TestResultsMigrationManagerImpl
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCase
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCaseImpl
import nl.rijksoverheid.ctr.design.BuildConfig
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecase.BuildConfigUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecase.HolderFeatureFlagUseCaseImpl
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase
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
            get(),
            isDebugApp(androidContext()),
            get()
        )
    }

    factory<TestResultsMigrationManager> { TestResultsMigrationManagerImpl(get()) }

    factory<ReturnToExternalAppUseCase> {
        ReturnToExternalAppUseCaseImpl(get())
    }

    factory<BuildConfigUseCase> {
        BuildConfigUseCaseImpl()
    }

    factory<HolderFeatureFlagUseCase> {
        HolderFeatureFlagUseCaseImpl(get())
    }
}

private fun isDebugApp(androidContext: Context) =
    BuildConfig.DEBUG || androidContext.packageName == "nl.rijksoverheid.ctr.holder.acc"
