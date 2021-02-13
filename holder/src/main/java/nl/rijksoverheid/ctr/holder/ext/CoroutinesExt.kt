package nl.rijksoverheid.ctr.holder.ext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@ExperimentalCoroutinesApi
fun tickFlow(millis: Long) = callbackFlow<Int> {
    val timer = Timer()
    var time = 0
    timer.scheduleAtFixedRate(
        object : TimerTask() {
            override fun run() {
                try {
                    offer(time)
                } catch (e: Exception) {
                }
                time += 1
            }
        },
        0,
        millis
    )
    awaitClose {
        timer.cancel()
    }
}
