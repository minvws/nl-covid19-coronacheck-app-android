package nl.rijksoverheid.ctr.holder

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import nl.rijksoverheid.ctr.shared.util.AndroidUtil
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val holderPreferenceModule = module {
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
