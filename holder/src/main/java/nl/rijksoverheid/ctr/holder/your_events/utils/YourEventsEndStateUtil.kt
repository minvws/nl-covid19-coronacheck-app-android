/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.your_events.utils

import android.content.Context
import nl.rijksoverheid.ctr.holder.utils.StringUtil
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndState
import nl.rijksoverheid.ctr.holder.your_events.models.YourEventsEndStateWithCustomTitle
import nl.rijksoverheid.ctr.shared.models.WeCouldnCreateCertificateException

interface YourEventsEndStateUtil {
    fun getEndState(context: Context, hints: List<String>): YourEventsEndState
}

class YourEventsEndStateUtilImpl(
    private val stringUtil: StringUtil
) : YourEventsEndStateUtil {
    override fun getEndState(context: Context, hints: List<String>): YourEventsEndState {
        val endStateFromHints = hintsToEndState(hints)
        return if (endStateFromHints != YourEventsEndState.None) {
            endStateFromHints
        } else {
            val localisedHints =
                hints.map { stringUtil.getStringFromResourceName(it) }.filterNot { it.isEmpty() }
            if (localisedHints.isEmpty()) {
                YourEventsEndState.None
            } else {
                YourEventsEndState.Hints(localisedHints)
            }
        }
    }

    private fun hintsToEndState(hints: List<String>): YourEventsEndState {
        val anyRecoveryCreated =
            hints.contains("domestic_recovery_created") || hints.contains("international_recovery_created")
        val allRecoveriesCreated =
            hints.contains("domestic_recovery_created") && hints.contains("international_recovery_created")
        val anyRecoveryRejected =
            hints.contains("domestic_recovery_rejected") || hints.contains("international_recovery_rejected")
        val anyVaccinationCreated =
            hints.contains("domestic_vaccination_created") || hints.contains("international_vaccination_created")
        val allVaccinationsCreated =
            hints.contains("domestic_vaccination_created") && hints.contains("international_vaccination_created")
        val anyVaccinationRejected =
            hints.contains("domestic_vaccination_rejected") || hints.contains("international_vaccination_rejected")
        val anyNegativeTestCreated =
            hints.contains("domestic_negativetest_created") || hints.contains("international_negativetest_created")
        val anyNegativeTestRejected =
            hints.contains("domestic_negativetest_rejected") || hints.contains("international_negativetest_rejected")

        if (hints.contains("domestic_vaccinationassessment_rejected")) {
            return YourEventsEndState.WeCouldntMakeACertificateError(
                WeCouldnCreateCertificateException("063")
            )
        }

        if (allRecoveriesCreated && hints.contains("vaccination_dose_correction_applied")) {
            return if (allVaccinationsCreated) {
                YourEventsEndStateWithCustomTitle.VaccinationsAndRecovery
            } else {
                YourEventsEndStateWithCustomTitle.RecoveryAndDosisCorrection
            }
        }

        if (!anyVaccinationCreated && !anyVaccinationRejected) {
            if (hints.contains("negativetest_without_vaccinationassessment")) {
                return YourEventsEndState.NegativeTestResultAddedAndNowAddVisitorAssessment
            } else if (hints.contains("vaccinationassessment_missing_supporting_negative_test") ||
                hints.contains("domestic_vaccinationassessment_created")
            ) {
                return YourEventsEndState.None
            }

            if (anyNegativeTestCreated) {
                return YourEventsEndState.None
            } else if (anyNegativeTestRejected) {
                return YourEventsEndState.WeCouldntMakeACertificateError(
                    WeCouldnCreateCertificateException("062")
                )
            }

            return if (anyRecoveryCreated) {
                YourEventsEndState.None
            } else if (anyRecoveryRejected && hints.contains("vaccination_dose_correction_applied")) {
                YourEventsEndStateWithCustomTitle.NoRecoveryButDosisCorrection
            } else if (anyRecoveryRejected && hints.contains("vaccination_dose_correction_not_applied")) {
                YourEventsEndStateWithCustomTitle.RecoveryTooOld
            } else if (anyRecoveryRejected) {
                YourEventsEndState.WeCouldntMakeACertificateError(
                    WeCouldnCreateCertificateException("061")
                )
            } else {
                YourEventsEndState.None
            }
        }

        if (anyRecoveryCreated && !anyVaccinationCreated && !anyVaccinationRejected) {
            return YourEventsEndState.None
        }

        if (!anyVaccinationRejected && !anyRecoveryCreated) {
            return YourEventsEndState.None
        }

        if (anyRecoveryCreated) {
            return if (anyVaccinationCreated) {
                if (hints.contains("domestic_vaccination_created")) {
                    YourEventsEndStateWithCustomTitle.VaccinationsAndRecovery
                } else {
                    YourEventsEndStateWithCustomTitle.InternationalVaccinationAndRecovery
                }
            } else {
                YourEventsEndStateWithCustomTitle.RecoveryOnly
            }
        }

        if (hints.contains("domestic_vaccination_rejected") && hints.contains("international_vaccination_rejected")) {
            return YourEventsEndState.WeCouldntMakeACertificateError(
                WeCouldnCreateCertificateException("059")
            )
        }

        if (hints.contains("domestic_vaccination_rejected")) {
            return YourEventsEndStateWithCustomTitle.InternationalQROnly
        }

        if (hints.containsAll(
                listOf(
                    "domestic_recovery_rejected",
                    "international_recovery_rejected",
                    "domestic_vaccination_rejected",
                    "international_vaccination_rejected",
                    "vaccination_dose_correction_not_applied"
                )
            )
        ) {
            return YourEventsEndState.WeCouldntMakeACertificateError(
                WeCouldnCreateCertificateException("060")
            )
        }

        return YourEventsEndState.None
    }
}
