package nl.rijksoverheid.ctr.shared.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import androidx.security.crypto.MasterKeys
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

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
    fun isNetworkAvailable(): Boolean
    fun getConnectivityManager(): ConnectivityManager
    fun generateRandomKey(): String
    fun getFirstInstallTime(): OffsetDateTime
}

class AndroidUtilImpl(private val context: Context) : AndroidUtil {
    override fun isSmallScreen(): Boolean {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp < 600 && configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                configuration.screenWidthDp < 600 && configuration.screenHeightDp < 600
    }

    override fun getMasterKeyAlias(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val activeNetworkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            // Check if we can access the network through wifi or cellular data
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            // Check for bluetooth pass-through just in case
            activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    override fun getConnectivityManager(): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    override fun generateRandomKey(): String = UUID.randomUUID().toString()

    override fun getFirstInstallTime(): OffsetDateTime {
        val millis = context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)
    }
}
