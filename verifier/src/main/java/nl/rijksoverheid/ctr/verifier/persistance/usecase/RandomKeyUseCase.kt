package nl.rijksoverheid.ctr.verifier.persistance.usecase

import nl.rijksoverheid.ctr.shared.ext.toHex
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * Generates a random key (can be used to encrypt the database)
 */
interface RandomKeyUseCase {
    fun exists(): Boolean
    fun get(): String
    fun persist()
}

class RandomKeyUseCaseImpl(
    private val persistenceManager: PersistenceManager,
    private val androidUtil: AndroidUtil): RandomKeyUseCase {
    override fun exists(): Boolean {
        return persistenceManager.getRandomKey() != null
    }

    override fun get(): String {
        return persistenceManager.getRandomKey() ?: error("Random key cannot be null")
    }

    override fun persist() {
        if (!exists()) {
            persistenceManager.saveRandomKey(androidUtil.generateRandomKey().toHex())
        }
    }
}