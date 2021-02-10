package nl.rijksoverheid.ctr.holder.usecase

import clmobile.Clmobile
import nl.rijksoverheid.ctr.holder.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.ext.successString

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class SecretKeyUseCase(
    private val persistenceManager: PersistenceManager
) {

    fun json(): String {
        return persistenceManager.getSecretKeyJson()
            ?: throw Exception("Secret key is not yet generated, persist first")
    }

    fun persist() {
        if (persistenceManager.getSecretKeyJson() == null) {
            persistenceManager.saveSecretKeyJson(json = Clmobile.generateHolderSk().successString())
        }
    }
}
