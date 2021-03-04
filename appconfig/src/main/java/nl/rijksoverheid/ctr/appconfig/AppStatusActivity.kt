/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.appconfig.databinding.ActivityAppStatusBinding
import nl.rijksoverheid.ctr.appconfig.model.AppStatus

class AppStatusActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_APP_STATUS = "EXTRA_APP_STATUS"

        fun launch(activity: Activity, appStatus: AppStatus) {
            val intent = Intent(activity, AppStatusActivity::class.java)
            intent.putExtra(EXTRA_APP_STATUS, appStatus)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    private val appStatusStrings by lazy { (application as AppStatusStringProvider).getAppStatusStrings() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppStatusBinding.inflate(layoutInflater)
        val appStatus = intent.getParcelableExtra<AppStatus>(EXTRA_APP_STATUS)

        setContentView(binding.root)

        when (appStatus) {
            is AppStatus.Deactivated -> {
                binding.bind(
                    appStatusStrings.appStatusDeactivatedTitle,
                    appStatusStrings.appStatusDeactivatedMessage,
                    appStatusStrings.appStatusDeactivatedAction
                ) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(appStatus.informationUrl)))
                }
            }
            is AppStatus.UpdateRequired -> {
                binding.bind(
                    appStatusStrings.appStatusUpdateRequiredTitle,
                    appStatusStrings.appStatusDeactivatedMessage,
                    appStatusStrings.appStatusUpdateRequiredAction
                ) {
                    openPlayStore()
                }
            }
            is AppStatus.InternetRequired -> {
                binding.bind(
                    appStatusStrings.appStatusInternetRequiredTitle,
                    appStatusStrings.appStatusInternetRequiredMessage,
                    appStatusStrings.appStatusInternetRequiredAction
                ) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.coronacheck.nl")
                        )
                    )
                }
            }
            else -> {
                /* nothing */
            }
        }

    }

    private fun openPlayStore() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            .setPackage("com.android.vending")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // fall back to browser intent
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}

private fun ActivityAppStatusBinding.bind(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes action: Int,
    onClick: () -> Unit
) {
    this.title.setText(title)
    this.message.setText(message)
    this.action.setText(action)
    this.action.setOnClickListener {
        onClick()
    }
}
