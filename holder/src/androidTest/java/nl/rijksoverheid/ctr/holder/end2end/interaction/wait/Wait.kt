/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */
// Based on https://github.com/AzimoLabs/ConditionWatcher
package nl.rijksoverheid.ctr.holder.end2end.interaction.wait

import timber.log.Timber

object Wait {

    private enum class State {
        ConditionNotMet,
        ConditionMet,
        Timeout
    }

    @Throws
    fun until(condition: Condition, timeoutLimit: Long, watchInterval: Long = 50) {
        var status = State.ConditionNotMet
        var elapsedTime: Long = 0
        do {
            if (condition.checkCondition() == true) {
                status = State.ConditionMet
            } else {
                elapsedTime += watchInterval
                Thread.sleep(watchInterval)
            }
            if (elapsedTime >= timeoutLimit) {
                Timber.tag("end2end").d("Waiting on '${condition.description}' timed out!")
                status = State.Timeout
                break
            }
        } while (status != State.ConditionMet)
        if (status == State.Timeout) throw Exception("Waiting until '${condition.description}' timed out")
    }
}
