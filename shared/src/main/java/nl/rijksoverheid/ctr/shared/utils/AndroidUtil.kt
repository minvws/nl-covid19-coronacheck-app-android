package nl.rijksoverheid.ctr.shared.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import androidx.security.crypto.MasterKeys

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface AndroidUtil {
    fun isSmallScreen(): Boolean
    fun getMasterKeyAlias(): String
    fun isFirstInstall(): Boolean
}

class AndroidUtilImpl(private val context: Context) : AndroidUtil {
    override fun isSmallScreen(): Boolean {
        return context.resources.displayMetrics.heightPixels <= 800 || context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    override fun getMasterKeyAlias(): String =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            try {
                MasterKeys.getOrCreate(
                    KeyGenParameterSpec.Builder(
                        "_coronacheck_security_master_key_",
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .setIsStrongBoxBacked(true)
                        .build()
                )
            } catch (e: StrongBoxUnavailableException) {
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            }
        } else {
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        }

    override fun isFirstInstall(): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val firstInstallTime = packageInfo.firstInstallTime
            val lastUpdateTime = packageInfo.lastUpdateTime
            firstInstallTime == lastUpdateTime
        } catch (exc: PackageManager.NameNotFoundException) {
            true
        }
    }
}
