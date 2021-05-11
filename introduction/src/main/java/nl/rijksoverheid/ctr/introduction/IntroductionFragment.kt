package nl.rijksoverheid.ctr.introduction

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import nl.rijksoverheid.ctr.introduction.ui.status.models.IntroductionStatus
import nl.rijksoverheid.ctr.introduction.ui.status.IntroductionStatusFragment

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionFragment : Fragment(R.layout.fragment_introduction) {

    companion object {
        private const val EXTRA_INTRODUCTION_STATUS = "EXTRA_INTRODUCTION_STATUS"

        fun getBundle(
            introductionStatus: IntroductionStatus
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_INTRODUCTION_STATUS, introductionStatus)
            return bundle
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.setGraph(
            R.navigation.introduction_nav_graph, IntroductionStatusFragment.getBundle(
                introductionStatus = arguments?.getParcelable(
                    EXTRA_INTRODUCTION_STATUS
                ) ?: error("IntroductionStatus should be set")
            )
        )
    }
}
