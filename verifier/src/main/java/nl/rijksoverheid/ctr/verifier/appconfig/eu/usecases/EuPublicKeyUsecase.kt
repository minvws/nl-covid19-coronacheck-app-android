/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.appconfig.eu.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.ctr.appconfig.eu.models.EuPublicKeysResult
import nl.rijksoverheid.ctr.appconfig.eu.repositories.EuPublicKeysRepository
import nl.rijksoverheid.ctr.verifier.persistance.PersistenceManager
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.OffsetDateTime

interface EuPublicKeyUsecase {
    suspend fun retrieveEuPublicKeys(): EuPublicKeysResult
    fun checkEuPublicKeysValidity(): Boolean
}

class EuPublicKeyUsecaseImpl(
    private val clock: Clock,
    private val euPublicKeysRepository: EuPublicKeysRepository,
    private val verifierPeristenceManager: PersistenceManager,
) : EuPublicKeyUsecase {
    override suspend fun retrieveEuPublicKeys()
            : EuPublicKeysResult = withContext(Dispatchers.IO) {
        try {
            val publicKeys = euPublicKeysRepository.getPublicKeys()
            val config = euPublicKeysRepository.config()
            val success = EuPublicKeysResult.Success(
                publicKeys = publicKeys,
                config = config,
            )
            verifierPeristenceManager.saveEuPublicKeysLastFetchedSeconds(
                OffsetDateTime.now(clock).toEpochSecond()
            )
            success
        } catch (e: IOException) {
            EuPublicKeysResult.Error
        } catch (e: HttpException) {
            EuPublicKeysResult.Error
        }
    }


    override fun checkEuPublicKeysValidity(): Boolean {
        return verifierPeristenceManager.getEuPublicKeysLastFetchedSeconds() + (24 * 60 * 60) >= OffsetDateTime.now(
            clock
        ).toEpochSecond()
    }

}