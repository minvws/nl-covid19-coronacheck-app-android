package nl.rijksoverheid.ctr.holder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.appconfig.appConfigModule
import nl.rijksoverheid.ctr.design.designModule
import nl.rijksoverheid.ctr.holder.modules.appModule
import nl.rijksoverheid.ctr.holder.modules.errorsModule
import nl.rijksoverheid.ctr.holder.modules.holderPreferenceModule
import nl.rijksoverheid.ctr.holder.modules.storageModule
import nl.rijksoverheid.ctr.holder.modules.utilsModule
import nl.rijksoverheid.ctr.holder.modules.viewModels
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.shared.SharedApplication
import nl.rijksoverheid.ctr.shared.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
open class HolderApplication : SharedApplication() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    open fun coroutineScopeBlock(block: suspend () -> Unit) {
        coroutineScope.launch { block() }
    }

    private val holderModules = listOf(
        storageModule,
        utilsModule,
        viewModels,
        appModule,
        errorsModule(BuildConfig.FLAVOR),
    ).toTypedArray()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@HolderApplication)
            modules(
                *holderModules,
                sharedModule,
                appConfigModule(BuildConfig.CDN_API_URL, "holder", BuildConfig.VERSION_CODE),
                *getAdditionalModules().toTypedArray(),
                designModule
            )
        }

        coroutineScopeBlock {
            HolderDatabase.deleteDatabase(this)
        }
    }

    override fun getAdditionalModules(): List<Module> {
        return listOf(holderPreferenceModule)
    }
}
