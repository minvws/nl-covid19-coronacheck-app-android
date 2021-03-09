/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.ConfigRepository
import nl.rijksoverheid.ctr.appconfig.model.ConfigResult
import retrofit2.HttpException
import java.io.IOException

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface AppConfigUseCase {
    suspend fun get(): ConfigResult
}

class AppConfigUseCaseImpl(private val configRepository: ConfigRepository) : AppConfigUseCase {
    override suspend fun get(): ConfigResult = withContext(Dispatchers.IO) {
        try {
            ConfigResult.Success(
                appConfig = configRepository.getConfig(),
                publicKeys = configRepository.getPublicKeys()
            )
        } catch (e: IOException) {
            ConfigResult.NetworkError
        } catch (e: HttpException) {
            ConfigResult.ServerError
        }
    }
}
