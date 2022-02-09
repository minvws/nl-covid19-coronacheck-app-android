/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.shared

import android.content.SharedPreferences
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

interface DisclosurePolicyPersistenceManager {
    fun getDebugDisclosurePolicy(): DisclosurePolicy?
    fun setDebugDisclosurePolicy(policy: DisclosurePolicy?)
}

class DisclosurePolicyPersistenceManagerImpl(
    private val sharedPreferences: SharedPreferences
) : DisclosurePolicyPersistenceManager {

    companion object {
        const val DEBUG_DISCLOSURE_POLICY = "DEBUG_DISCLOSURE_POLICY"
    }

    override fun getDebugDisclosurePolicy(): DisclosurePolicy? {
        val value = sharedPreferences.getString(DEBUG_DISCLOSURE_POLICY, "") ?: ""
        return DisclosurePolicy.fromString(value)
    }

    override fun setDebugDisclosurePolicy(policy: DisclosurePolicy?) {
        sharedPreferences.edit().putString(DEBUG_DISCLOSURE_POLICY, policy?.stringValue).commit()
    }
}