/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.honeywellscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_DATA_COLLECTION_SERVICE
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_DATA_PROCESSOR_SYMBOLOGY_ID_NONE

import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_ACTION_CLAIM_SCANNER
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_ACTION_RELEASE_SCANNER
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_EXTRA_SCANNER
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_EXTRA_PROFILE
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_EXTRA_PROPERTIES
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_INTENT_ACTION_BARCODE_DATA_FILTER_ACTION
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_DATA_INTENT
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_DATA_INTENT_ACTION
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_EDIT_DATA_PLUGIN
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_PREFIX
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_SUFFIX
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_DATA_PROCESSOR_SYMBOLOGY_PREFIX
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_QR_CODE_ENABLED
import nl.rijksoverheid.ctr.honeywellscanner.HoneywellConstants.HONEYWELL_PROPERTY_WEDGE_ENABLED


interface HoneywellManager {
    fun suspendScanner()
    fun resumeScanner()
    fun setupHoneywellScanner(onDatawedgeResultListener: (data: String) -> Unit)
    fun teardownHoneywellScanner()
    fun isHoneywellDevice() : Boolean
}

class HoneywellManagerImpl(
    private val context: Context
) : HoneywellManager {

    private var honeywellIntentSet = false
    private lateinit var onDatawedgeResultListener: (data: String) -> Unit

    private val dataWedgeResultBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra(HoneywellConstants.DATAWEDGE_INTENT_KEY_SYMBOLOGY)?.let {
                if (it == "s") { // if QR
                    intent.getStringExtra(HoneywellConstants.DATAWEDGE_INTENT_KEY_DATA)?.let {
                        // suspendScanner()
                        onDatawedgeResultListener.invoke(it)
                    }
                }
            }
        }
    }

    override fun isHoneywellDevice(): Boolean {
        return Build.MANUFACTURER == "Honeywell"
    }

    override fun suspendScanner() {
        context.sendBroadcast(
            Intent(HONEYWELL_INTENT_ACTION_RELEASE_SCANNER)
                .setPackage(HONEYWELL_DATA_COLLECTION_SERVICE)
        )
        context.unregisterReceiver(dataWedgeResultBroadcastReceiver)
    }

    override fun resumeScanner() {

        val resultFilter = IntentFilter()
        resultFilter.addAction(HONEYWELL_INTENT_ACTION_BARCODE_DATA_FILTER_ACTION)
        context.registerReceiver(dataWedgeResultBroadcastReceiver, resultFilter)


        val properties = Bundle()
        properties.putBoolean(HONEYWELL_PROPERTY_DATA_PROCESSOR_DATA_INTENT, true)
        properties.putString(
            HONEYWELL_PROPERTY_DATA_PROCESSOR_DATA_INTENT_ACTION,
            HONEYWELL_INTENT_ACTION_BARCODE_DATA_FILTER_ACTION
        )

        properties.putBoolean(HONEYWELL_PROPERTY_WEDGE_ENABLED, true)
        properties.putString(HONEYWELL_PROPERTY_DATA_PROCESSOR_PREFIX, "")
        properties.putString(HONEYWELL_PROPERTY_DATA_PROCESSOR_SUFFIX, "")
        properties.putString(
            HONEYWELL_PROPERTY_DATA_PROCESSOR_SYMBOLOGY_PREFIX,
            HONEYWELL_DATA_PROCESSOR_SYMBOLOGY_ID_NONE
        )
        properties.putBoolean(HONEYWELL_PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false)
        properties.putString(HONEYWELL_PROPERTY_DATA_PROCESSOR_EDIT_DATA_PLUGIN, "")

        properties.putBoolean(HONEYWELL_PROPERTY_QR_CODE_ENABLED, true)

        context.sendBroadcast(
            Intent(HONEYWELL_INTENT_ACTION_CLAIM_SCANNER)
                .setPackage(HONEYWELL_DATA_COLLECTION_SERVICE)
                .putExtra(HONEYWELL_INTENT_EXTRA_SCANNER, "dcs.scanner.imager")
                .putExtra(
                    HONEYWELL_INTENT_EXTRA_PROFILE,
                    "DEFAULT"
                ) // this name must be an existing profile, which is used as base. Do not change to a custom name unless it has been pre-staged on the device.
                .putExtra(HONEYWELL_INTENT_EXTRA_PROPERTIES, properties)
        )
    }

    override fun setupHoneywellScanner(onDatawedgeResultListener: (data: String) -> Unit) {
        if (isHoneywellDevice() && !honeywellIntentSet) {
            this.onDatawedgeResultListener = onDatawedgeResultListener

            honeywellIntentSet = true
        }
    }

    override fun teardownHoneywellScanner() {
        if (isHoneywellDevice() && (honeywellIntentSet)) {
            // suspendScanner()
            honeywellIntentSet = false
        }
    }

}