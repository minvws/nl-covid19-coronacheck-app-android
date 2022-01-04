/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.utils
import nl.rijksoverheid.ctr.holder.ui.myoverview.QrCodesFragment
import timber.log.Timber
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface QrCodesFragmentUtil {
    /**
     * If [QrCodesFragment] should close
     */
    fun shouldClose(credentialExpirationTimeSeconds: Long): Boolean
}

class QrCodesFragmentUtilImpl(
    private val utcClock: Clock
): QrCodesFragmentUtil {

    override fun shouldClose(credentialExpirationTimeSeconds: Long): Boolean {
        val now = Instant.now(utcClock)
        val expiration = Instant.ofEpochSecond(credentialExpirationTimeSeconds)
        return now.isAfter(expiration)
    }
}