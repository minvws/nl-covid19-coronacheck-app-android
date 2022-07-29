package nl.rijksoverheid.ctr.appconfig.models

import android.os.Parcelable
import java.io.Serializable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@Parcelize
data class AppUpdateData(
    val newFeatures: List<NewFeatureItem> = listOf(),
    val newTerms: NewTerms,
    val newFeatureVersion: Int? = null,
    val hideConsent: Boolean = false
) : Parcelable {

    @IgnoredOnParcel
    var savePolicyChangeSerialized: Serializable? = null
        private set

    fun setSavePolicyChange(f: () -> Unit) {
        savePolicyChangeSerialized = f as Serializable
    }

    @Suppress("UNCHECKED_CAST")
    fun savePolicyChange() = (savePolicyChangeSerialized as? () -> Unit)?.invoke()
}
