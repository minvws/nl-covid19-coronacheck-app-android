/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.device_secure

import android.app.KeyguardManager
import android.content.Context
import android.content.Context.KEYGUARD_SERVICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface DeviceSecureUseCase {
    suspend fun isDeviceSecure(): Boolean
}

class DeviceSecureUseCaseImpl(val context: Context) : DeviceSecureUseCase {
    override suspend fun isDeviceSecure(): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine {
                val keyguardManager = context.getSystemService(KEYGUARD_SERVICE) as KeyguardManager
                it.resume(keyguardManager.isDeviceSecure)
            }
        }
    }
}