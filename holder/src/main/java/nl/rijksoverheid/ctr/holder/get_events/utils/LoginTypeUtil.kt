package nl.rijksoverheid.ctr.holder.get_events.utils

import androidx.annotation.StringRes
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.get_events.models.LoginType
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteOriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface LoginTypeUtil {
    @StringRes
    fun getCanceledDialogTitle(loginType: LoginType): Int
    @StringRes
    fun getCanceledDialogDescription(loginType: LoginType, originType: RemoteOriginType): Int
    @StringRes
    fun getNoBrowserDialogDescription(loginType: LoginType): Int
}

class LoginTypeUtilImpl: LoginTypeUtil {
    override fun getCanceledDialogTitle(loginType: LoginType): Int {
        return when (loginType) {
            LoginType.Max -> R.string.holder_authentication_popup_digid_title
            LoginType.Pap -> R.string.holder_authentication_popup_portal_title
        }
    }

    override fun getCanceledDialogDescription(loginType: LoginType, originType: RemoteOriginType): Int {
        return when (loginType) {
            LoginType.Max -> if (originType == RemoteOriginType.Vaccination) {
                R.string.holder_authentication_popup_digid_message_vaccinationFlow
            } else {
                R.string.holder_authentication_popup_digid_message_testFlow
            }
            LoginType.Pap -> if (originType == RemoteOriginType.Vaccination) {
                R.string.holder_authentication_popup_portal_message_vaccinationFlow
            } else {
                R.string.holder_authentication_popup_portal_message_testFlow
            }
        }
    }

    override fun getNoBrowserDialogDescription(loginType: LoginType): Int {
        return when (loginType) {
            LoginType.Max -> R.string.holder_authentication_popup_digid
            LoginType.Pap -> R.string.holder_authentication_popup_portal
        }
    }
}
