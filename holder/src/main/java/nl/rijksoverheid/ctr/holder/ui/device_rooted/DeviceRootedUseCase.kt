package nl.rijksoverheid.ctr.appconfig.usecases

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DeviceRootedUseCase {

    /**
     * Checks if the phone is rooted
     * @return true if the phone is rooted, false if phone is not rooted
     */
    suspend fun isDeviceRooted(): Boolean
}

class DeviceRootedUseCaseImpl(private val context: Context) : DeviceRootedUseCase {

    override suspend fun isDeviceRooted(): Boolean {
        return withContext(Dispatchers.IO) {
            suspendCoroutine {
                try {
                    val rootBeer = RootBeer(context)
                    if (rootBeer.isRooted) {
                        it.resume(true)
                    } else {
                        it.resume(false)
                    }
                } catch (e: Exception) {
                    it.resume(false)
                }
            }
        }
    }
}
