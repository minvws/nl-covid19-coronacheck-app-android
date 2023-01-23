/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

// Based on https://github.com/AzimoLabs/ConditionWatcher

package nl.rijksoverheid.ctr.holder.end2end.wait

import timber.log.Timber

object Wait {

    private enum class State {
        ConditionNotMet,
        ConditionMet,
        Timeout
    }

    @Throws
    fun until(condition: Condition, timeoutLimit: Long = 5 * 1_000, watchInterval: Long = 200) {
        Timber.tag("end2end").d("Start waiting on '${condition.description}'")
        var status = State.ConditionNotMet
        var elapsedTime: Long = 0
        do {
            if (condition.checkCondition() == true) {
                Timber.tag("end2end").d("Condition met of '${condition.description}'")
                status = State.ConditionMet
            } else {
                elapsedTime += watchInterval
                Timber.tag("end2end").d("Sleeping")
                Thread.sleep(watchInterval)
            }
            if (elapsedTime >= timeoutLimit) {
                Timber.tag("end2end").d("Waiting timed out")
                status = State.Timeout
                break
            }
        } while (status != State.ConditionMet)
        if (status == State.Timeout) throw Exception("Waiting until '${condition.description}' timed out")
    }
}
