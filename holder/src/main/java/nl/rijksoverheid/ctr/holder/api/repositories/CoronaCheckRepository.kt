package nl.rijksoverheid.ctr.holder.api.repositories

import android.util.Base64
import nl.rijksoverheid.ctr.api.factory.NetworkRequestResultFactory
import nl.rijksoverheid.ctr.holder.api.HolderApiClient
import nl.rijksoverheid.ctr.holder.api.HolderApiClientUtil
import nl.rijksoverheid.ctr.holder.api.RemoteConfigApiClient
import nl.rijksoverheid.ctr.holder.api.post.GetCouplingData
import nl.rijksoverheid.ctr.holder.api.post.GetCredentialsPostData
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteAccessTokens
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteConfigProviders
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.models.HolderFlow
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.holder.paper_proof.models.RemoteCouplingResponse
import nl.rijksoverheid.ctr.holder.your_events.models.RemoteGreenCards
import nl.rijksoverheid.ctr.holder.your_events.models.RemotePrepareIssue
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CoronaCheckRepository {
    suspend fun configProviders(): NetworkRequestResult<RemoteConfigProviders>
    suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens>
    suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String,
        flow: Flow
    ): NetworkRequestResult<RemoteGreenCards>

    suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue>
    suspend fun getCoupling(credential: String, couplingCode: String): NetworkRequestResult<RemoteCouplingResponse>
}

open class CoronaCheckRepositoryImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val holderApiClientUtil: HolderApiClientUtil,
    private val remoteConfigApiClient: RemoteConfigApiClient,
    private val networkRequestResultFactory: NetworkRequestResultFactory
) : CoronaCheckRepository {

    private fun getHolderApiClient(): HolderApiClient {
        val backendTlsCertificates = cachedAppConfigUseCase.getCachedAppConfig().backendTLSCertificates
        val certificateBytes = backendTlsCertificates.map { it.toByteArray() }
        return holderApiClientUtil.client(certificateBytes)
    }

    override suspend fun configProviders(): NetworkRequestResult<RemoteConfigProviders> {
        return networkRequestResultFactory.createResult(HolderStep.ConfigProvidersNetworkRequest) {
            remoteConfigApiClient.getConfigCtp()
        }
    }

    override suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens> {
        return networkRequestResultFactory.createResult(HolderStep.AccessTokensNetworkRequest) {
            getHolderApiClient().getAccessTokens(authorization = "Bearer $jwt")
        }
    }

    override suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String,
        flow: Flow
    ): NetworkRequestResult<RemoteGreenCards> {
        return networkRequestResultFactory.createResult(HolderStep.GetCredentialsNetworkRequest) {
            getHolderApiClient().getCredentials(
                data = GetCredentialsPostData(
                    stoken = stoken,
                    events = events,
                    issueCommitmentMessage = Base64.encodeToString(
                        issueCommitmentMessage.toByteArray(),
                        Base64.NO_WRAP
                    ),
                    flows = when (flow) {
                        is HolderFlow.Vaccination -> listOf("vaccination")
                        is HolderFlow.Recovery -> listOf("positivetest")
                        is HolderFlow.CommercialTest, is HolderFlow.DigidTest -> listOf("negativetest")
                        is HolderFlow.VaccinationAndPositiveTest -> listOf("vaccination", "positivetest")
                        is HolderFlow.VaccinationAssessment -> listOf("vaccinationassessment")
                        is HolderFlow.HkviScanned -> {
                            // Hkvi is a flow where you scanned a paper qr which holds one event. That event determines the backend flow.
                            when (flow.remoteProtocol.events?.first()) {
                                is RemoteEventVaccination -> listOf("vaccination")
                                is RemoteEventNegativeTest -> listOf("negativetest")
                                is RemoteEventPositiveTest -> listOf("positivetest")
                                is RemoteEventVaccinationAssessment -> listOf("vaccinationassessment")
                                else -> listOf("refresh")
                            }
                        }
                        else -> listOf()
                    }
                )
            )
        }
    }

    override suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue> {
        return networkRequestResultFactory.createResult(HolderStep.PrepareIssueNetworkRequest) {
            getHolderApiClient().getPrepareIssue()
        }
    }

    override suspend fun getCoupling(
        credential: String,
        couplingCode: String
    ): NetworkRequestResult<RemoteCouplingResponse> {
        return networkRequestResultFactory.createResult(HolderStep.CouplingNetworkRequest) {
            getHolderApiClient().getCoupling(
                data = GetCouplingData(
                    credential = credential,
                    couplingCode = couplingCode
                )
            )
        }
    }
}
