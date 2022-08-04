/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.qrcodes.utils

import java.time.Clock
import java.time.Instant
import nl.rijksoverheid.ctr.holder.qrcodes.QrCodesFragment
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType

interface QrCodesFragmentUtil {
    /**
     * If [QrCodesFragment] should close
     */
    fun shouldClose(credentialExpirationTimeSeconds: Long, type: GreenCardType): Boolean
}

class QrCodesFragmentUtilImpl(
    private val utcClock: Clock
) : QrCodesFragmentUtil {

    override fun shouldClose(credentialExpirationTimeSeconds: Long, type: GreenCardType): Boolean {
        if (type == GreenCardType.Eu) {
            return false
        }
        val now = Instant.now(utcClock)
        val expiration = Instant.ofEpochSecond(credentialExpirationTimeSeconds)
        return now.isAfter(expiration)
    }
}
