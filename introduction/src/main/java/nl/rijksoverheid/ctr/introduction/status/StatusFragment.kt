package nl.rijksoverheid.ctr.introduction.status

import android.os.Bundle
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class StatusFragment : Fragment() {
    private val coronaCheckApp by lazy { requireActivity().application as CoronaCheckApp }
    private val introductionViewModel: IntroductionViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        introductionViewModel.introductionFinishedLiveData.observe(
            this,
            EventObserver {
                if (it) {
                    coronaCheckApp.getIntroductionData().skipIntroductionCallback.invoke(this)
                } else {
                    coronaCheckApp.getIntroductionData().launchIntroductionCallback.invoke(
                        requireActivity()
                    )
                }
            })

        introductionViewModel.getIntroductionState()
    }
}
