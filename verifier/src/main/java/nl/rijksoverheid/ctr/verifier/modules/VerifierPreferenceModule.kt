package nl.rijksoverheid.ctr.verifier.modules

import androidx.security.crypto.EncryptedSharedPreferences
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val verifierPreferenceModule = module {
    single {
        val androidUtil = get<AndroidUtil>()

        EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            androidUtil.getMasterKeyAlias(),
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
