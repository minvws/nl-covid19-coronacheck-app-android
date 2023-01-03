package nl.rijksoverheid.ctr.holder.modules

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import java.io.File
import java.security.KeyStore
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
val holderPreferenceModule = module {
    single {
        val androidUtil = get<AndroidUtil>()
        val sharedPreferencesFileName = "secret_shared_prefs"
        val masterKeyAlias = androidUtil.getMasterKeyAlias()

        fun clearSharedPreferences() {
            androidContext().getSharedPreferences(sharedPreferencesFileName, Context.MODE_PRIVATE)
                .edit().clear()
                .apply()
        }

        fun createSharedPreferences() = EncryptedSharedPreferences.create(
            sharedPreferencesFileName,
            masterKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        fun deleteSharedPreferences() {
            try {
                val sharedPrefsFile =
                    File("${androidContext().filesDir.parent}/shared_prefs/$sharedPreferencesFileName.xml")

                clearSharedPreferences()

                if (sharedPrefsFile.exists()) {
                    sharedPrefsFile.delete()
                }

                // Delete the master key
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                keyStore.deleteEntry(AndroidUtil.KEYSTORE_ALIAS)
            } catch (e: Exception) {
                // no op
            }
        }

        try {
            createSharedPreferences()
        } catch (exception: Exception) {
            // Android Keystore occasionally corrupts the master key on certain devices cause of faulty OEM firmware/hardware
            // There's nothing we can do to prevent it, so we can only reset the user's keychain to at least be able to start the app
            // Workaround [https://github.com/google/tink/issues/535#issuecomment-912170221]
            // Issue Tracker - https://issuetracker.google.com/issues/176215143?pli=1
            deleteSharedPreferences()
            createSharedPreferences()
        }
    }
}
