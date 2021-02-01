package nl.rijksoverheid.ctr.holder.usecases

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import nl.rijksoverheid.ctr.holder.repositories.AuthenticationRepository
import nl.rijksoverheid.ctr.holder.repositories.HolderRepository
import nl.rijksoverheid.ctr.shared.repositories.EventRepository
import nl.rijksoverheid.ctr.shared.usecases.SignatureValidUseCase
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class HolderQrCodeUseCase(
    private val authenticationRepository: AuthenticationRepository,
    private val eventRepository: EventRepository,
    private val holderRepository: HolderRepository,
    private val commitmentMessageUseCase: CommitmentMessageUseCase,
    private val eventValidUseCase: EventValidUseCase,
    private val allowedTestResultForEventUseCase: AllowedTestResultForEventUseCase,
    private val signatureValidUseCase: SignatureValidUseCase,
    private val generateHolderQrCodeUseCase: GenerateHolderQrCodeUseCase
) {

    suspend fun qrCode(activity: AppCompatActivity, qrCodeWidth: Int, qrCodeHeight: Int): Bitmap {
        val remoteNonce = holderRepository.remoteNonce()
        val commitmentMessage = commitmentMessageUseCase.json(
            nonce =
            remoteNonce.nonce
        )
        Timber.i("Received commitment message $commitmentMessage")

        val accessToken = authenticationRepository.login(activity)
        Timber.i("Received access token $accessToken")

        val testResults = eventRepository.testResults(accessToken)
        val remoteEvent = eventRepository.remoteEvent("d9ff36de-2357-4fa6-a64e-1569aa57bf1c")
        val issuers = eventRepository.issuers()

        eventValidUseCase.checkValid(
            issuers = issuers.issuers,
            remoteEvent = remoteEvent
        )

        val allowedTestResult = allowedTestResultForEventUseCase.get(
            event = remoteEvent.event,
            testResults = testResults
        )

        signatureValidUseCase.checkValid(
            issuers = issuers.issuers,
            signature = allowedTestResult.testResultSignature.signature,
            data = allowedTestResult.testResult
        )

        return generateHolderQrCodeUseCase.bitmap(
            event = remoteEvent.event,
            allowedTestResult = allowedTestResult,
            qrCodeWidth = qrCodeWidth,
            qrCodeHeight = qrCodeHeight
        )
    }
}
