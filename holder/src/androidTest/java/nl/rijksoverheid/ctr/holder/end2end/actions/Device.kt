/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end.actions

import android.app.Instrumentation
import android.content.Intent
import android.provider.Settings
import androidx.test.uiautomator.UiDevice
import nl.rijksoverheid.ctr.holder.end2end.interaction.tapOnElementWithContentDescription
import timber.log.Timber

// Based on https://stackoverflow.com/a/58359193
@Suppress("deprecation")
fun Instrumentation.setAirplaneMode(enable: Boolean) {
    val expectedState = if (enable) 1 else 0
    val currentState = Settings.Global.getInt(this.context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0)
    Timber.tag("end2end").d("Airplane mode state is currently '$currentState', expected state is '$expectedState'")
    if (expectedState == currentState) return
    val device = UiDevice.getInstance(this)
    device.openQuickSettings()
    device.tapOnElementWithContentDescription("Vliegtuigmodus")
    context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
}
