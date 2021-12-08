package nl.rijksoverheid.ctr.verifier.ui.scanqr

import android.os.CountDownTimer
import android.text.format.DateUtils
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyUseCase


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScannerStateCountDownTimer(
    private val verificationPolicyUseCase: VerificationPolicyUseCase,
    private val onTick: (String) -> Unit,
    private val onFinish: (VerificationPolicyState) -> Unit,
) : CountDownTimer(
    verificationPolicyUseCase.getRemainingSecondsLocked() * timerIntervalMs,
    timerIntervalMs
) {
    override fun onTick(millisUntilFinished: Long) {
        onTick(
            DateUtils.formatElapsedTime(
                millisUntilFinished / 1000L
            )
        )
    }

    override fun onFinish() {
        onFinish(verificationPolicyUseCase.getState())
    }

    companion object {
        private const val timerIntervalMs = 1000L
    }
}