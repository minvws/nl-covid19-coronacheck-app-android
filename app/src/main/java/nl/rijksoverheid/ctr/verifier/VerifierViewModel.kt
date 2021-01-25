package nl.rijksoverheid.ctr.verifier

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.citizen.models.CustomerQr
import nl.rijksoverheid.ctr.data.api.TestApiClient
import nl.rijksoverheid.ctr.data.models.AgentQR
import nl.rijksoverheid.ctr.data.models.Result
import nl.rijksoverheid.ctr.ext.toObject
import nl.rijksoverheid.ctr.usecases.IsCitizenAllowedUseCase
import nl.rijksoverheid.ctr.usecases.IsTestResultSignatureValidUseCase

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class VerifierViewModel(
    private val api: TestApiClient,
    private val moshi: Moshi,
    private val isCitizenAllowedUseCase: IsCitizenAllowedUseCase,
    private val isTestResultSignatureValidUseCase: IsTestResultSignatureValidUseCase
) : ViewModel() {

    val citizenAllowedLiveData = MutableLiveData<Result<Boolean>>()

    fun validateCitizen(customerQrJson: String) {
        viewModelScope.launch {
            try {
                val customerQr = customerQrJson.toObject<CustomerQr>(moshi)
                val issuers = api.getIssuers()
                val agentQr =
                    "{\"agent\":{\"event\":{\"name\":\"Friday Night\",\"private_key\":\"BWbaGr8FH2w9ndg8fP0Q8uBafSMZPpY83eG7Ha6hD4w=\",\"valid_from\":1611008598,\"valid_to\":1611584139,\"type\":{\"uuid\":\"e2255ea4-2140-44c8-bdf0-33da60debf70\",\"name\":\"Friday Night\"},\"valid_tests\":[{\"name\":\"PCR\",\"uuid\":\"58d8e4b1-f890-4a2f-b810-0b775caa2149\",\"max_validity\":604800},{\"name\":\"Breathalyzer\",\"uuid\":\"e4ecba8d-1f87-4d72-b698-b3136e7c1141\",\"max_validity\":10800}]}},\"agent_signature\":\"jm4EJm7s9Xtx1N0SqMpgParF0N1IPsrbS\\/475DJPmaiIJXXwVhANRVfcXTZg2Hhrju512u8TxXrdypRf3Pq4Dg==\"}".toObject<AgentQR>(
                        moshi
                    )

                val isCitizenAllowedResult = isCitizenAllowedUseCase.isAllowed(
                    customerQr = customerQr,
                    agent = agentQr.agent
                )

                if (isCitizenAllowedResult is IsCitizenAllowedUseCase.IsCitizenAllowedResult.NotAllowed) {
                    throw Exception(isCitizenAllowedResult.reason)
                }

                val citizenPayload =
                    (isCitizenAllowedResult as IsCitizenAllowedUseCase.IsCitizenAllowedResult.Allowed).payload

                val isTestResultSignatureValidResult = isTestResultSignatureValidUseCase.isValid(
                    issuers = issuers.issuers,
                    validTestResultForEvent = citizenPayload.test,
                    validTestResultSignature = citizenPayload.testSignature
                )

                if (isTestResultSignatureValidResult is IsTestResultSignatureValidUseCase.IsTestResultValidResult.Invalid) {
                    throw Exception(isTestResultSignatureValidResult.reason)
                }

                citizenAllowedLiveData.postValue(Result.Success(true))
            } catch (e: Exception) {
                citizenAllowedLiveData.postValue(Result.Failed(e))
            }
        }
    }
}
