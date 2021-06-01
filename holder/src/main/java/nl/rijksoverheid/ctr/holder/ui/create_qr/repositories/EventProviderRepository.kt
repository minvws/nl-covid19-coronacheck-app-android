package nl.rijksoverheid.ctr.holder.ui.create_qr.repositories

import nl.rijksoverheid.ctr.api.interceptors.SigningCertificate
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.TestProviderApiClient
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsVaccinations
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEventsNegativeTests
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteUnomi
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface EventProviderRepository {
    suspend fun unomi(
        url: String,
        token: String
    ): RemoteUnomi

    suspend fun vaccinationEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsVaccinations>

    suspend fun negativeTestEvent(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsNegativeTests>
}

class EventProviderRepositoryImpl(
    private val testProviderApiClient: TestProviderApiClient
) : EventProviderRepository {

    override suspend fun unomi(url: String, token: String): RemoteUnomi {
        return testProviderApiClient
            .unomi(
                url = url,
                authorization = "Bearer $token",
            )
    }

    override suspend fun vaccinationEvents(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsVaccinations> {
        return testProviderApiClient.vaccinationEvents(
            url = url,
            authorization = "Bearer $token",
            certificate = SigningCertificate(signingCertificateBytes)
        )
    }

    override suspend fun negativeTestEvent(
        url: String,
        token: String,
        signingCertificateBytes: ByteArray
    ): SignedResponseWithModel<RemoteEventsNegativeTests> {
        return testProviderApiClient.negativeTestEvents(
            url = url,
            authorization = "Bearer $token",
            certificate = SigningCertificate(signingCertificateBytes)
        )
    }
}
