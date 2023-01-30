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
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.Flow
import nl.rijksoverheid.ctr.shared.models.NetworkRequestResult
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import retrofit2.Converter

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

interface CoronaCheckRepository {
    suspend fun configProviders(useCache: Boolean = true): NetworkRequestResult<RemoteConfigProviders>
    suspend fun accessTokens(jwt: String): NetworkRequestResult<RemoteAccessTokens>
    suspend fun getGreenCards(
        stoken: String,
        events: List<String>,
        issueCommitmentMessage: String,
        flow: Flow
    ): NetworkRequestResult<RemoteGreenCards>

    suspend fun getPrepareIssue(): NetworkRequestResult<RemotePrepareIssue>
    suspend fun getCoupling(
        credential: String,
        couplingCode: String
    ): NetworkRequestResult<RemoteCouplingResponse>
}

private const val FUZZY_MATCHING_ERROR = 99790
private const val VACCINATION_BACKEND_FLOW = "vaccination"
private const val POSITIVE_TEST_BACKEND_FLOW = "positivetest"
private const val NEGATIVE_TEST_BACKEND_FLOW = "negativetest"
private const val VACCINATION_ASSESSMENT_BACKEND_FLOW = "vaccinationassessment"
private const val REFRESH_BACKEND_FLOW = "refresh"

open class CoronaCheckRepositoryImpl(
    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase,
    private val holderApiClientUtil: HolderApiClientUtil,
    private val remoteConfigApiClient: RemoteConfigApiClient,
    private val errorResponseBodyConverter: Converter<ResponseBody, CoronaCheckErrorResponse>,
    private val responseBodyConverter: Converter<ResponseBody, RemoteGreenCards>,
    private val networkRequestResultFactory: NetworkRequestResultFactory
) : CoronaCheckRepository {

    private var cachedConfigProvidersResult: NetworkRequestResult<RemoteConfigProviders>? = null

    private fun getHolderApiClient(): HolderApiClient {
        val backendTlsCertificates =
            cachedAppConfigUseCase.getCachedAppConfig().backendTLSCertificates
        val certificateBytes = backendTlsCertificates.map { it.toByteArray() }
        return holderApiClientUtil.client(certificateBytes)
    }

    override suspend fun configProviders(useCache: Boolean): NetworkRequestResult<RemoteConfigProviders> {
        if (useCache) {
            cachedConfigProvidersResult?.takeIf {
                it is NetworkRequestResult.Success
            }?.let { return it }
        }

        val result = networkRequestResultFactory.createResult(HolderStep.ConfigProvidersNetworkRequest) {
            remoteConfigApiClient.getConfigCtp()
        }
        cachedConfigProvidersResult = result
        return result
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
        return networkRequestResultFactory.createResult(
            step = HolderStep.GetCredentialsNetworkRequest,
            networkCall = {
                getHolderApiClient().getCredentials(
                    data = GetCredentialsPostData(
                        stoken = stoken,
                        events = events,
                        issueCommitmentMessage = Base64.encodeToString(
                            issueCommitmentMessage.toByteArray(),
                            Base64.NO_WRAP
                        ),
                        flows = when (flow) {
                            is HolderFlow.Vaccination -> listOf(VACCINATION_BACKEND_FLOW)
                            is HolderFlow.Recovery -> listOf(POSITIVE_TEST_BACKEND_FLOW)
                            is HolderFlow.CommercialTest, is HolderFlow.DigidTest -> listOf(
                                NEGATIVE_TEST_BACKEND_FLOW
                            )
                            is HolderFlow.VaccinationAndPositiveTest -> listOf(
                                VACCINATION_BACKEND_FLOW,
                                POSITIVE_TEST_BACKEND_FLOW
                            )
                            is HolderFlow.VaccinationAssessment -> listOf(
                                VACCINATION_ASSESSMENT_BACKEND_FLOW
                            )
                            is HolderFlow.Refresh -> listOf(REFRESH_BACKEND_FLOW)
                            is HolderFlow.HkviScanned -> {
                                // Hkvi is a flow where you scanned a paper qr which holds one event. That event determines the backend flow.
                                when (flow.remoteProtocol.events?.first()) {
                                    is RemoteEventVaccination -> listOf(VACCINATION_BACKEND_FLOW)
                                    is RemoteEventNegativeTest -> listOf(NEGATIVE_TEST_BACKEND_FLOW)
                                    is RemoteEventPositiveTest -> listOf(POSITIVE_TEST_BACKEND_FLOW)
                                    is RemoteEventVaccinationAssessment -> listOf(
                                        VACCINATION_ASSESSMENT_BACKEND_FLOW
                                    )
                                    else -> listOf(REFRESH_BACKEND_FLOW)
                                }
                            }
                            else -> listOf()
                        }
                    )
                )
            },
            interceptHttpError = {
                it.response()?.errorBody()?.let { errorBody ->
                    val errorBodyBuffer = errorBody.source().buffer.clone()
                    errorResponseBodyConverter.convert(errorBody)?.let { coronaErrorResponse ->
                        if (coronaErrorResponse.code == FUZZY_MATCHING_ERROR) {
                            val errorBodyClone = errorBodyBuffer.asResponseBody(
                                errorBody.contentType(),
                                errorBody.contentLength()
                            )
                            responseBodyConverter.convert(errorBodyClone)
                        } else {
                            null
                        }
                    }
                }
            }
        )
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
