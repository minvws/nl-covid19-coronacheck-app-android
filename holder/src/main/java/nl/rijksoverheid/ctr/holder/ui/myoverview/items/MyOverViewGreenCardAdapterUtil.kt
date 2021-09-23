package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import android.widget.TextView
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayShortMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.GreenCardUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface MyOverViewGreenCardAdapterUtil {
    fun setContent(greenCard: GreenCard, originStates: List<OriginState>, viewBinding: ViewBindingWrapper)
}

class MyOverViewGreenCardAdapterUtilImpl(
    private val context: Context,
    private val credentialUtil: CredentialUtil,
    private val testResultAdapterItemUtil: TestResultAdapterItemUtil,
    private val greenCardUtil: GreenCardUtil,
): MyOverViewGreenCardAdapterUtil {
    override fun setContent(greenCard: GreenCard, originStates: List<OriginState>, viewBinding: ViewBindingWrapper) {
        val greenCardType = greenCard.greenCardEntity.type
        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                // European card only has one origin
                val originState = originStates.first()
                val showTimeFrom = originState.origin.type == OriginType.Recovery || (originState.origin.type == OriginType.Vaccination && greenCardType == GreenCardType.Domestic)
                val origin = originState.origin
                when (origin.type) {
                    is OriginType.Test -> {
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            title = "${context.getString(R.string.qr_card_test_domestic)} PCR (${credentialUtil.getTestTypeForEuropeanCredentials(greenCard.credentialEntities)})",
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            originState = originState,
                            showTime = showTimeFrom,
                            subtitle = "${context.getString(R.string.qr_card_test_title_eu)} ${origin.eventTime.formatDateTime(context)}",
                        )
                    }

                    is OriginType.Vaccination -> {
                        val getCurrentDosesString: (String, String) -> String = { currentDose: String, sumDoses: String ->
                            context.getString(
                                R.string.qr_card_vaccination_doses,
                                currentDose, sumDoses
                            )
                        }
                        val doses = credentialUtil.getVaccinationDosesForEuropeanCredentials(greenCard.credentialEntities, getCurrentDosesString)
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            title = "${context.getString(R.string.qr_card_vaccination_title_domestic)} $doses",
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            // force a valid origin, as we need to allow the user to view the QR
                            // and when is valid from, it depends from the country going to
                            originState = OriginState.Valid(greenCard.origins.first()),
                            showTime = showTimeFrom,
                            subtitle = "${context.getString(R.string.qr_card_vaccination_title_eu)} ${origin.eventTime.toLocalDate().formatDayMonthYear()}",
                        )
                    }

                    is OriginType.Recovery -> {
                        setOriginTitle(
                            textView = viewBinding.proof1Title,
                            title = context.getString(R.string.qr_card_recovery_title_domestic),
                        )

                        setOriginSubtitle(
                            textView = viewBinding.proof1Subtitle,
                            originState = originState,
                            showTime = showTimeFrom,
                            subtitle = context.getString(R.string.qr_card_validity_valid, origin.expirationTime.toLocalDate().formatDayShortMonthYear()),
                        )
                    }

                }
            }
            is GreenCardType.Domestic -> {
                originStates.forEach { originState ->
                    val showTimeFrom = originState.origin.type == OriginType.Recovery || (originState.origin.type == OriginType.Vaccination && greenCardType == GreenCardType.Domestic)
                    val origin = originState.origin
                    when (origin.type) {
                        is OriginType.Test -> {
                            setOriginTitle(
                                textView = viewBinding.proof3Title,
                                title = context.getString(R.string.qr_card_test_domestic),
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof3Subtitle,
                                originState = originState,
                            showTime = showTimeFrom,
                                subtitle = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.formatDateTime(context)
                                )
                            )
                        }
                        is OriginType.Vaccination -> {
                            setOriginTitle(
                                textView = viewBinding.proof1Title,
                                title = context.getString(R.string.qr_card_vaccination_title_domestic),
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof1Subtitle,
                                originState = originState,
                            showTime = showTimeFrom,
                                subtitle = context.getString(R.string.qr_card_validity_future_from, origin.validFrom.toLocalDate().formatDayMonthYear(), "")
                            )
                        }
                        is OriginType.Recovery -> {
                            setOriginTitle(
                                textView = viewBinding.proof2Title,
                                title = context.getString(R.string.qr_card_recovery_title_domestic),
                            )

                            setOriginSubtitle(
                                textView = viewBinding.proof2Subtitle,
                                originState = originState,
                            showTime = showTimeFrom,
                                subtitle = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.toLocalDate().formatDayShortMonthYear()
                                )
                            )
                        }
                    }
                }

                // If there is only one origin we can show a countdown if the green card almost expires
                when (val expireCountDownResult =
                    testResultAdapterItemUtil.getExpireCountdownText(expireDate = greenCardUtil.getExpireDate(greenCard))) {
                    is TestResultAdapterItemUtil.ExpireCountDown.Hide -> {
                        viewBinding.expiresIn.visibility = View.GONE
                    }
                    is TestResultAdapterItemUtil.ExpireCountDown.Show -> {
                        viewBinding.expiresIn.visibility = View.VISIBLE
                        if (expireCountDownResult.hoursLeft == 0L) {
                            viewBinding.expiresIn.text = context.getString(
                                R.string.my_overview_test_result_expires_in_minutes,
                                expireCountDownResult.minutesLeft.toString()
                            )
                        } else {
                             viewBinding.expiresIn.text = context.getString(
                                R.string.my_overview_test_result_expires_in_hours_minutes,
                                expireCountDownResult.hoursLeft.toString(),
                                expireCountDownResult.minutesLeft.toString()
                            )
                        }
                    }
                }
            }
        }

        val becomesValidAutomatically = originStates.size == 1 &&
                originStates.first() is OriginState.Future &&
                (originStates.first().origin.type == OriginType.Recovery || (originStates.first().origin.type == OriginType.Vaccination && greenCard.greenCardEntity.type == GreenCardType.Domestic))
        if (becomesValidAutomatically) {
            viewBinding.expiresIn.visibility = View.VISIBLE
            viewBinding.expiresIn.text = context.getString(R.string.qr_card_validity_future)
        }
    }

    private fun setOriginTitle(
        textView: TextView,
        title: String,
    ) {
        textView.text = title
        textView.visibility = View.VISIBLE
    }

    private fun setOriginSubtitle(
        textView: TextView,
        originState: OriginState,
        showTime: Boolean,
        subtitle: String,
    ) {
        val context = textView.context

        when (originState) {
            is OriginState.Future -> {
                val showUntil = originState.origin.type == OriginType.Recovery

                val validFromDateTime = originState.origin.validFrom
                val validFrom = if (showTime) {
                    validFromDateTime.formatDayMonthTime(context)
                } else {
                    validFromDateTime.toLocalDate().formatDayMonthYear()
                }
                textView.text = context.getString(R.string.qr_card_validity_future_from, validFrom, if (showUntil) {
                    val until = originState.origin.expirationTime.toLocalDate().formatDayMonthYear()
                    context.getString(R.string.qr_card_validity_future_until, until)
                } else {
                    ""
                })

                textView.visibility = View.VISIBLE
            }
            is OriginState.Valid -> {
                textView.text = subtitle
                textView.visibility = View.VISIBLE
            }
            is OriginState.Expired -> {
                // Should be filtered out and never reach here
            }
        }
    }
}