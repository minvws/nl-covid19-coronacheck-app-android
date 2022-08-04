/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.verifier.managers

import nl.rijksoverheid.ctr.appconfig.usecases.AppStatusUseCase
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.shared.BuildConfigUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DeeplinkManager {
    fun set(returnUri: String)
    fun getReturnUri(): String?
    fun removeReturnUri()
}

/**
 * store here a return uri if app opened via a deeplink
 * and we want to go back there after finished scanning
 */
class DeeplinkManagerImpl(
    private val introductionPersistenceManager: IntroductionPersistenceManager,
    private val buildConfigUseCase: BuildConfigUseCase,
    private val appStatusUseCase: AppStatusUseCase
) : DeeplinkManager {
    private var returnUri: String? = null

    override fun set(returnUri: String) {
        this.returnUri = returnUri
    }

    override fun getReturnUri(): String? {
        // if introduction not finished yet or app is not active,
        // don't allow the already opened [ScanQrFragment] to consume it
        return if (
            introductionPersistenceManager.getIntroductionFinished() &&
            appStatusUseCase.isAppActive(buildConfigUseCase.getVersionCode())
        ) {
            returnUri
        } else {
            null
        }
    }

    override fun removeReturnUri() {
        returnUri = null
    }
}
