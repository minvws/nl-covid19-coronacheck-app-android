package nl.rijksoverheid.ctr.holder.modules

import android.content.Context
import androidx.work.WorkerFactory
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.DeviceRootedUseCaseImpl
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCase
import nl.rijksoverheid.ctr.appconfig.usecases.ReturnToExternalAppUseCaseImpl
import nl.rijksoverheid.ctr.design.BuildConfig
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCase
import nl.rijksoverheid.ctr.holder.ui.device_secure.DeviceSecureUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.priority_notification.PriorityNotificationUseCase
import nl.rijksoverheid.ctr.holder.ui.priority_notification.PriorityNotificationUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecases.BuildConfigUseCaseImpl
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCaseImpl
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCaseImpl
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
    factory<PriorityNotificationUseCase> { PriorityNotificationUseCaseImpl(get(), get()) }
    factory<DeviceRootedUseCase> { DeviceRootedUseCaseImpl(androidContext()) }
    factory<DeviceSecureUseCase> { DeviceSecureUseCaseImpl(androidContext()) }
    factory<HolderCachedAppConfigUseCase> {
        HolderCachedAppConfigUseCaseImpl(
            get()
        )
    }

    factory<ReturnToExternalAppUseCase> {
        ReturnToExternalAppUseCaseImpl(get())
    }

    factory<BuildConfigUseCase> {
        BuildConfigUseCaseImpl()
    }

    factory<HolderFeatureFlagUseCase> {
        HolderFeatureFlagUseCaseImpl(get(), get())
    }

}

private fun isDebugApp(androidContext: Context) =
    BuildConfig.DEBUG || androidContext.packageName == "nl.rijksoverheid.ctr.holder.acc"
