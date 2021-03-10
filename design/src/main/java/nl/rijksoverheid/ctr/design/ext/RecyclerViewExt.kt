package nl.rijksoverheid.ctr.shared.ext

import androidx.recyclerview.widget.RecyclerView

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: (RecyclerView) -> Unit
) = post(
    object : Runnable {
        override fun run() {
            if (isAnimating) {
                itemAnimator?.isRunning {
                    post(this)
                }
            } else {
                callback(this@executeAfterAllAnimationsAreFinished)
            }
        }
    }
)
