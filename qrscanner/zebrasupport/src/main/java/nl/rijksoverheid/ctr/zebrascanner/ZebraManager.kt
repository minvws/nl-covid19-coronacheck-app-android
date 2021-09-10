/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.zebrascanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_COMMAND_IDENTIFIER
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_INTENT_GET_PROFILE_LIST
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_INTENT_RESULT_RECEIVER
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_SCANNER_INPUT_PLUGIN
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_SEND_RESULT
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.DATAWEDGE_SET_CONFIG
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_ACTIVITY_LIST
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_APP_LIST
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_CONFIG_MODE
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_DECODE_QRCODE
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_INTENT_ACTION
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_INTENT_CATEGORY
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_INTENT_DELIVERY
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_INTENT_OUTPUT_ENABLED
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_KEYSTROKE_OUTPUT_ENABLED
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PACKAGE_NAME
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PARAM_LIST
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PLUGIN_CONFIG
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PLUGIN_NAME
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PROFILE_ENABLED
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_PROFILE_NAME
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_RESET_CONFIG
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.PROFILE_BUNDLE_SCANNER_INPUT_ENABLED
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.ZEBRA_INTENT_DATAWEDGE_ACTION
import nl.rijksoverheid.ctr.zebrascanner.ZebraConstants.ZEBRA_INTENT_FILTER_ACTION


interface ZebraManager {
    fun setupDataWedgeProfile()
    fun createDataWedgeProfile()
    fun suspendScanner()
    fun resumeScanner()
    fun setupZebraScanner(onDatawedgeResultListener: (data: String) -> Unit)
    fun teardownZebraScanner()
    fun isZebraDevice() : Boolean
}

class ZebraManagerImpl(
    private val context: Context
) : ZebraManager {

    private var zebraIntentSet = false
    private lateinit var onDatawedgeResultListener: (data: String) -> Unit

    private val dataWedgeProfileResultBroadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.getStringArrayExtra(ZebraConstants.DATAWEDGE_INTENT_RESULT_GET_PROFILE_LIST)
                    ?.let {
                        var profileExist = false
                        if (!it.isEmpty()) {
                            it.forEach {
                                if (it.equals(ZebraConstants.DATAWEDGE_PROFILE_NAME))
                                    profileExist = true
                            }
                        }
                        if (!profileExist) {
                            createDataWedgeProfile()
                        }
                    }
            }
        }

    private val dataWedgeResultBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.getStringExtra(ZebraConstants.DATAWEDGE_INTENT_KEY_DATA)?.let {
                suspendScanner()
                onDatawedgeResultListener.invoke(it)
            }
        }
    }

    override fun isZebraDevice(): Boolean {
        return Build.MANUFACTURER == "Zebra Technologies"
    }

    override fun setupDataWedgeProfile() {
        val i = Intent()
        i.action = ZEBRA_INTENT_DATAWEDGE_ACTION
        i.putExtra(DATAWEDGE_INTENT_GET_PROFILE_LIST, "")
        context.sendBroadcast(i)
    }

    override fun createDataWedgeProfile() {

        val setConfigBundle = Bundle()
        setConfigBundle.putString(
            PROFILE_BUNDLE_PROFILE_NAME,
            ZebraConstants.DATAWEDGE_PROFILE_NAME
        )
        setConfigBundle.putString(PROFILE_BUNDLE_PROFILE_ENABLED, "true")
        setConfigBundle.putString(PROFILE_BUNDLE_CONFIG_MODE, "CREATE_IF_NOT_EXIST")
        setConfigBundle.putString(PROFILE_BUNDLE_RESET_CONFIG, "false")

        // Associate profile with this app
        val appConfig = Bundle()
        appConfig.putString(PROFILE_BUNDLE_PACKAGE_NAME, context.getPackageName())
        appConfig.putStringArray(PROFILE_BUNDLE_ACTIVITY_LIST, arrayOf("*"))
        setConfigBundle.putParcelableArray(PROFILE_BUNDLE_APP_LIST, arrayOf(appConfig))

        // Configure scanner input parameters, decoders for barcode type to be sent to this app
        val barcodeInputConfig = Bundle()
        barcodeInputConfig.putString(PROFILE_BUNDLE_PLUGIN_NAME, "BARCODE")
        barcodeInputConfig.putString(PROFILE_BUNDLE_RESET_CONFIG, "true")

        val barcodeInputProps = Bundle()
        barcodeInputProps.putString(PROFILE_BUNDLE_SCANNER_INPUT_ENABLED, "true")
        barcodeInputProps.putString(PROFILE_BUNDLE_DECODE_QRCODE, "true")
        barcodeInputConfig.putBundle(PROFILE_BUNDLE_PARAM_LIST, barcodeInputProps)

        // Configure keystroke output for captured data to be sent to this app
        val keystrokeConfig = Bundle()
        keystrokeConfig.putString(PROFILE_BUNDLE_PLUGIN_NAME, "KEYSTROKE")
        keystrokeConfig.putString(PROFILE_BUNDLE_RESET_CONFIG, "true")
        val keystrokeProps = Bundle()
        keystrokeProps.putString(PROFILE_BUNDLE_KEYSTROKE_OUTPUT_ENABLED, "false")
        keystrokeConfig.putBundle(PROFILE_BUNDLE_PARAM_LIST, keystrokeProps)

        // Configure intent output for captured data to be sent to this app
        val intentConfig = Bundle()
        intentConfig.putString(PROFILE_BUNDLE_PLUGIN_NAME, "INTENT")
        intentConfig.putString(PROFILE_BUNDLE_RESET_CONFIG, "true")
        val intentProps = Bundle()
        intentProps.putString(PROFILE_BUNDLE_INTENT_OUTPUT_ENABLED, "true")
        intentProps.putString(PROFILE_BUNDLE_INTENT_ACTION, ZEBRA_INTENT_FILTER_ACTION)
        intentProps.putString(PROFILE_BUNDLE_INTENT_CATEGORY, "android.intent.category.DEFAULT")
        intentProps.putString(PROFILE_BUNDLE_INTENT_DELIVERY, "2")
        intentConfig.putBundle(PROFILE_BUNDLE_PARAM_LIST, intentProps)

        // Add configurations into a collection
        val configBundles: ArrayList<Parcelable> = ArrayList()
        configBundles.add(barcodeInputConfig)
        configBundles.add(keystrokeConfig)
        configBundles.add(intentConfig)
        setConfigBundle.putParcelableArrayList(PROFILE_BUNDLE_PLUGIN_CONFIG, configBundles)

        // Broadcast the intent
        val intent = Intent()
        intent.action = ZEBRA_INTENT_DATAWEDGE_ACTION
        intent.putExtra(DATAWEDGE_SET_CONFIG, setConfigBundle)
        context.sendBroadcast(intent)
    }

    override fun suspendScanner() {
        val i = Intent()
        i.action = ZEBRA_INTENT_DATAWEDGE_ACTION
        i.putExtra(DATAWEDGE_SCANNER_INPUT_PLUGIN, "SUSPEND_PLUGIN")
        i.putExtra(DATAWEDGE_SEND_RESULT, "true")
        i.putExtra(DATAWEDGE_COMMAND_IDENTIFIER, "MY_SUSPEND_SCANNER") //Unique identifier
        context.sendBroadcast(i)
    }

    override fun resumeScanner() {
        val i = Intent()
        i.action = "com.symbol.datawedge.api.ACTION"
        i.putExtra(DATAWEDGE_SCANNER_INPUT_PLUGIN, "RESUME_PLUGIN")
        i.putExtra(DATAWEDGE_SEND_RESULT, "true")
        i.putExtra(DATAWEDGE_COMMAND_IDENTIFIER, "MY_RESUME_SCANNER") //Unique identifier
        context.sendBroadcast(i)
    }

    override fun setupZebraScanner(onDatawedgeResultListener: (data: String) -> Unit) {
        if (isZebraDevice() && !zebraIntentSet) {
            this.onDatawedgeResultListener = onDatawedgeResultListener

            val filter = IntentFilter()
            filter.addCategory(Intent.CATEGORY_DEFAULT)
            filter.addAction(ZEBRA_INTENT_FILTER_ACTION)
            context.registerReceiver(dataWedgeResultBroadcastReceiver, filter)

            val resultFilter = IntentFilter()
            resultFilter.addAction(DATAWEDGE_INTENT_RESULT_RECEIVER)
            resultFilter.addCategory("android.intent.category.DEFAULT")
            context.registerReceiver(dataWedgeProfileResultBroadcastReceiver, resultFilter)

            zebraIntentSet = true
            setupDataWedgeProfile()
        }
    }

    override fun teardownZebraScanner() {
        if (isZebraDevice() && (zebraIntentSet)) {
            suspendScanner()
            context.unregisterReceiver(dataWedgeResultBroadcastReceiver)
            context.unregisterReceiver(dataWedgeProfileResultBroadcastReceiver)
            zebraIntentSet = false
        }
    }

}