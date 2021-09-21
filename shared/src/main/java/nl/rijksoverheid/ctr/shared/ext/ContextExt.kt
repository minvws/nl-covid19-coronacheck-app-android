/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


fun Context.isNetworkAvailable(): Boolean {
    // Check if network is available through the ConnectivityManager since NetworkInfo is deprecated
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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