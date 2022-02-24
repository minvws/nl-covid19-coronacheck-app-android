/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.shared.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import java.time.Clock
import java.time.Instant

abstract class AutoCloseFragment(contentLayoutId: Int) : Fragment(contentLayoutId) {

    companion object {
        private const val FRAGMENT_CREATED_TIMESTAMP = "FRAGMENT_CREATED_TIMESTAMP"
    }

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = object: Runnable {
        override fun run() {
            checkShouldClose()
            autoCloseHandler.postDelayed(this, 1000)
        }
    }

    // Time when this fragment was created
    private var fragmentCreatedTimestamp: Long = Instant.now(Clock.systemUTC()).toEpochMilli()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore time from saved instance state so the time when the fragment was created is correct
        fragmentCreatedTimestamp = savedInstanceState?.getLong(FRAGMENT_CREATED_TIMESTAMP, fragmentCreatedTimestamp) ?: fragmentCreatedTimestamp
    }

    override fun onResume() {
        super.onResume()
        checkShouldClose()
        autoCloseHandler.postDelayed(autoCloseRunnable, 1000)
    }

    override fun onPause() {
        super.onPause()
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
    }

    /**
     * Checks if this fragment should close based on [aliveForMilliseconds]
     * Navigates to [navigateToCloseAt] when finished
     */
    private fun checkShouldClose() {
        val fragmentCreatedInstant = Instant.ofEpochMilli(fragmentCreatedTimestamp)
        val nowInstant = Instant.now(Clock.systemUTC())

        if (fragmentCreatedInstant.plusMillis(aliveForMilliseconds()).isBefore(nowInstant)) {
            navigateToCloseAt()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save time this fragment was created
        outState.putLong(FRAGMENT_CREATED_TIMESTAMP, fragmentCreatedTimestamp)
    }

    abstract fun aliveForMilliseconds(): Long
    abstract fun navigateToCloseAt()
}