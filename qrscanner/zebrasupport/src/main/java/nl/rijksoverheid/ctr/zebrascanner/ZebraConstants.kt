/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.zebrascanner

object ZebraConstants {
    const val ZEBRA_INTENT_FILTER_ACTION = "nl.rijksoverheid.ctr.qrscanner.ACTION"
    const val ZEBRA_INTENT_DATAWEDGE_ACTION = "com.symbol.datawedge.api.ACTION"
    const val DATAWEDGE_INTENT_KEY_SOURCE = "com.symbol.datawedge.source"
    const val DATAWEDGE_INTENT_KEY_LABEL_TYPE = "com.symbol.datawedge.label_type"
    const val DATAWEDGE_INTENT_KEY_DATA = "com.symbol.datawedge.data_string"
    const val DATAWEDGE_INTENT_RESULT_RECEIVER = "com.symbol.datawedge.api.RESULT_ACTION"
    const val DATAWEDGE_INTENT_GET_PROFILE_LIST = "com.symbol.datawedge.api.GET_PROFILES_LIST"
    const val DATAWEDGE_INTENT_RESULT_GET_PROFILE_LIST = "com.symbol.datawedge.api.RESULT_GET_PROFILES_LIST"
    const val DATAWEDGE_PROFILE_NAME = "nl.rijksoverheid.ctr.verifier"

    // Profile configuration
    const val PROFILE_BUNDLE_PLUGIN_CONFIG = "PLUGIN_CONFIG"
    const val PROFILE_BUNDLE_PROFILE_NAME = "PROFILE_NAME"
    const val PROFILE_BUNDLE_PROFILE_ENABLED = "PROFILE_ENABLED"
    const val PROFILE_BUNDLE_CONFIG_MODE = "CONFIG_MODE"
    const val PROFILE_BUNDLE_RESET_CONFIG = "RESET_CONFIG"
    const val PROFILE_BUNDLE_PACKAGE_NAME = "PACKAGE_NAME"
    const val PROFILE_BUNDLE_ACTIVITY_LIST = "ACTIVITY_LIST"
    const val PROFILE_BUNDLE_APP_LIST = "APP_LIST"
    const val PROFILE_BUNDLE_PLUGIN_NAME= "PLUGIN_NAME"
    const val PROFILE_BUNDLE_SCANNER_INPUT_ENABLED= "scanner_input_enabled"
    const val PROFILE_BUNDLE_DECODE_QRCODE= "decoder_qrcode"
    const val PROFILE_BUNDLE_PARAM_LIST= "PARAM_LIST"
    const val PROFILE_BUNDLE_KEYSTROKE_OUTPUT_ENABLED= "keystroke_output_enabled"
    const val PROFILE_BUNDLE_INTENT_OUTPUT_ENABLED= "intent_output_enabled"
    const val PROFILE_BUNDLE_INTENT_ACTION= "intent_action"
    const val PROFILE_BUNDLE_INTENT_CATEGORY= "intent_category"
    const val PROFILE_BUNDLE_INTENT_DELIVERY= "intent_delivery"
    const val DATAWEDGE_SCANNER_INPUT_PLUGIN= "com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN"
    const val DATAWEDGE_SEND_RESULT= "SEND_RESULT"
    const val DATAWEDGE_COMMAND_IDENTIFIER= "COMMAND_IDENTIFIER"
    const val DATAWEDGE_SET_CONFIG = "com.symbol.datawedge.api.SET_CONFIG"
}