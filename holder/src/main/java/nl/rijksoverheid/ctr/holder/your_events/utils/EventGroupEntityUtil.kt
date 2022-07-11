/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase

interface EventGroupEntityUtil {
    fun getProviderName(providerIdentifier: String): String
}

class EventGroupEntityUtilImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase
): EventGroupEntityUtil {

    override fun getProviderName(providerIdentifier: String): String {
        // Some provider identifiers have a unique appended to it, the first part is the actual provider identifier
        val providerIdentifierWithoutUnique = providerIdentifier.split("_").first()

        return cachedAppConfigUseCase.getCachedAppConfig().providerIdentifiers
            .firstOrNull { it.code == providerIdentifierWithoutUnique }
            ?.name
            ?: providerIdentifier
    }
}