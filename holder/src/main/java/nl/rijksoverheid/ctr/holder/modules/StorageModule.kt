package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.persistence.SharedPreferencesPersistenceManager
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncer
import nl.rijksoverheid.ctr.persistence.database.HolderDatabaseSyncerImpl
import nl.rijksoverheid.ctr.shared.models.Environment
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
            get(),
            Environment.get(androidContext()) is Environment.Prod
        )
    }

    factory<HolderDatabaseSyncer> {
        HolderDatabaseSyncerImpl(get(), get(), get(), get(), get(), get(), get(), get(), get())
    }

    single<PersistenceManager> {
        SharedPreferencesPersistenceManager(
            get()
        )
    }
}
