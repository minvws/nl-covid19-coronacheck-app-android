package nl.rijksoverheid.ctr.appconfig

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class AppStatusFragment : Fragment() {

    private val args: AppStatusFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (val status = args.appStatus) {
            is AppStatus.UpdateRequired,
            is AppStatus.Deactivated,
            is AppStatus.Archived,
            is AppStatus.LaunchError -> navigateSafety(
                AppStatusFragmentDirections.actionAppLocked(status)
            )
            is AppStatus.Error -> navigateSafety(
                AppStatusFragmentDirections.actionAppLocked(status)
            )
            is AppStatus.NewFeatures -> {
                navigateSafety(
                    AppStatusFragmentDirections.actionNavNewFeatures(status.appUpdateData)
                )
            }
            is AppStatus.ConsentNeeded -> {
                navigateSafety(
                    AppStatusFragmentDirections.actionNewTerms(status.appUpdateData)
                )
            }
            is AppStatus.NoActionRequired,
            is AppStatus.UpdateRecommended -> Unit
        }
    }
}
