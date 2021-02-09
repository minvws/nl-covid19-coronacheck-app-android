package nl.rijksoverheid.ctr.holder.usecase

import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class IntroductionUseCase(private val persistenceManager: PersistenceManager) {

    fun onboardingFinished() = persistenceManager.getOnboardingFinished()
    fun setOnboardingFinished() = persistenceManager.saveOnboardingFinished()
    fun privacyPolicyFinished() = persistenceManager.getPrivacyPolicyFinished()
    fun setPrivacyPolicyFinished() = persistenceManager.savePrivacyPolicyFinished()
}
