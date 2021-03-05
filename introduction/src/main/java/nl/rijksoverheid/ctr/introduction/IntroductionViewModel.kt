package nl.rijksoverheid.ctr.introduction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.ctr.holder.persistence.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.shared.livedata.Event

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class IntroductionViewModel : ViewModel() {
    abstract fun introductionFinished(): Boolean
    abstract fun saveIntroductionFinished()
}

class IntroductionViewModelImpl(private val introductionPersistenceManager: IntroductionPersistenceManager) :
    IntroductionViewModel() {

    override fun introductionFinished(): Boolean {
        return introductionPersistenceManager.getIntroductionFinished()
    }

    override fun saveIntroductionFinished() {
        introductionPersistenceManager.saveIntroductionFinished()
    }

}
