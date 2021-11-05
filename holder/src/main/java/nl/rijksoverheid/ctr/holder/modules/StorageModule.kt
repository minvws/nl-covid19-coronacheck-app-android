package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.holder.persistence.database.HolderDatabaseSyncerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val storageModule = module {
    single {
        HolderDatabase.createInstance(
            androidContext(),
            get(),
            androidContext().packageName == "nl.rijksoverheid.ctr.holder"
        )
    }

    factory<HolderDatabaseSyncer> { HolderDatabaseSyncerImpl(get(), get(), get(), get(), get(), get()) }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }
}
