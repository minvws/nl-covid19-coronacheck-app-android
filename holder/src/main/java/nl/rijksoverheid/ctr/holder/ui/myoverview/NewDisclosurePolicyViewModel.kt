/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

abstract class NewDisclosurePolicyViewModel : ViewModel() {
    val disclosurePolicyLiveData: LiveData<DisclosurePolicy> = MutableLiveData()

   abstract fun init()
   abstract fun onNextClicked()
}

class NewDisclosurePolicyViewModelImpl : NewDisclosurePolicyViewModel() {

    override fun init() {

    }

    override fun onNextClicked() {

    }
}