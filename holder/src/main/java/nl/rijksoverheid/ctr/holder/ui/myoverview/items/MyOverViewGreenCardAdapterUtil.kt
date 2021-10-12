package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayShortMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
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
    fun setContent(
        viewBinding: ViewBindingWrapper,
        cards: List<AdapterCard>,
    )
}

class MyOverViewGreenCardAdapterUtilImpl(
    private val context: Context,
    private val credentialUtil: CredentialUtil,
    private val testResultAdapterItemUtil: TestResultAdapterItemUtil,
    private val greenCardUtil: GreenCardUtil,
) : MyOverViewGreenCardAdapterUtil {

    override fun setContent(
        viewBinding: ViewBindingWrapper,
        cards: List<AdapterCard>,
    ) {
        val greenCardType = cards.first().greenCard.greenCardEntity.type
        cards.forEach { card ->
            val it = card.greenCard
            when (it.greenCardEntity.type) {
                is GreenCardType.Eu -> {
                    // European card only has one origin
                    val originState = card.originStates.first()
                    val origin = originState.origin
                    when (origin.type) {
                        is OriginType.Test -> {
                            viewBinding.title.text =
                                context.getString(R.string.my_overview_test_result_title)
                            setEuTestOrigin(
                                viewBinding, it, originState, greenCardType, origin
                            )
                        }
                        is OriginType.Vaccination -> {
                            viewBinding.title.text =
                                context.getString(R.string.my_overview_green_card_vaccination_title)
                            setEuVaccinationOrigin(
                                viewBinding, it, originState, greenCardType, origin
                            )
                        }
                        is OriginType.Recovery -> {
                            viewBinding.title.text =
                                context.getString(R.string.my_overview_test_result_title)
                            setRecoveryOrigin(viewBinding, originState, greenCardType, origin)
                        }
                    }
                }
                is GreenCardType.Domestic -> {
                    viewBinding.title.text =
                        context.getString(R.string.my_overview_test_result_title)
                    card.originStates
                        .sortedBy { state -> state.origin.type.order }
                        .forEach { originState ->
                            val origin = originState.origin
                            when (origin.type) {
                                is OriginType.Vaccination -> setDomesticVaccinationOrigin(
                                    viewBinding, originState, greenCardType, origin
                                )
                                is OriginType.Recovery -> setRecoveryOrigin(
                                    viewBinding, originState, greenCardType, origin
                                )
                                is OriginType.Test -> setDomesticTestOrigin(
                                    viewBinding, originState, greenCardType, origin
                                )
                            }
                        }

                    // If there is only one origin we can show a countdown if the green card almost expires
                    when (val expireCountDownResult =
                        testResultAdapterItemUtil.getExpireCountdownText(
                            expireDate = greenCardUtil.getExpireDate(it)
                        )) {
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
        }

        val originStates = cards.first().originStates
        val becomesValidAutomatically = originStates.size == 1 &&
                originStates.first() is OriginState.Future &&
                shouldShowTimeSubtitle(originStates.first(), cards.first().greenCard.greenCardEntity.type)
        if (becomesValidAutomatically) {
            viewBinding.expiresIn.visibility = View.VISIBLE
            viewBinding.expiresIn.text = context.getString(R.string.qr_card_validity_future)
        }
    }

    private fun setDomesticTestOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        greenCardType: GreenCardType,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = context.getString(R.string.qr_card_test_domestic),
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = shouldShowTimeSubtitle(originState, greenCardType),
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.formatDateTime(context)
            )
        )
    }

    private fun setDomesticVaccinationOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        greenCardType: GreenCardType,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = context.getString(R.string.qr_card_vaccination_title_domestic),
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = shouldShowTimeSubtitle(originState, greenCardType),
            subtitle = context.getString(
                R.string.qr_card_validity_future_from,
                origin.validFrom.toLocalDate().formatDayMonthYear(),
                ""
            )
        )
    }

    private fun setRecoveryOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        greenCardType: GreenCardType,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = context.getString(R.string.qr_card_recovery_title_domestic),
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = shouldShowTimeSubtitle(originState, greenCardType),
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayShortMonthYear()
            ),
        )
    }

    private fun setEuVaccinationOrigin(
        viewBinding: ViewBindingWrapper,
        greenCard: GreenCard,
        originState: OriginState,
        greenCardType: GreenCardType,
        origin: OriginEntity
    ) {
        val getCurrentDosesString: (String, String) -> String =
            { currentDose: String, sumDoses: String ->
                context.getString(
                    R.string.qr_card_vaccination_doses,
                    currentDose, sumDoses
                )
            }
        val doses = credentialUtil.getVaccinationDosesForEuropeanCredentials(
            greenCard.credentialEntities,
            getCurrentDosesString
        )
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = doses,
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            // force a valid origin, as we need to allow the user to view the QR
            // and when is valid from, it depends from the country going to
            originState = OriginState.Valid(greenCard.origins.first()),
            showTime = shouldShowTimeSubtitle(originState, greenCardType),
            subtitle = "${context.getString(R.string.qr_card_vaccination_title_eu)} ${
                origin.eventTime.toLocalDate().formatDayMonthYear()
            }",
        )
    }

    private fun setEuTestOrigin(
        viewBinding: ViewBindingWrapper,
        greenCard: GreenCard,
        originState: OriginState,
        greenCardType: GreenCardType,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = "${context.getString(R.string.qr_card_test_domestic)} PCR (${
                credentialUtil.getTestTypeForEuropeanCredentials(
                    greenCard.credentialEntities
                )
            })",
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = shouldShowTimeSubtitle(originState, greenCardType),
            subtitle = "${context.getString(R.string.qr_card_test_title_eu)} ${
                origin.eventTime.formatDateTime(
                    context
                )
            }",
        )
    }

    private fun shouldShowTimeSubtitle(
        originState: OriginState,
        greenCardType: GreenCardType
    ) = originState.origin.type == OriginType.Recovery ||
            (originState.origin.type == OriginType.Vaccination && greenCardType == GreenCardType.Domestic)

    private fun setOriginTitle(
        descriptionLayout: LinearLayout,
        title: String,
    ) {
        descriptionLayout.addView(
            TextView(context).apply {
                setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
                text = title
            }
        )
    }

    private fun setOriginSubtitle(
        descriptionLayout: LinearLayout,
        originState: OriginState,
        showTime: Boolean,
        subtitle: String,
    ) {
        val textView = TextView(context).apply {
            setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
        }

        when (originState) {
            is OriginState.Future -> {
                val showUntil = originState.origin.type == OriginType.Recovery

                val validFromDateTime = originState.origin.validFrom
                val validFrom = if (showTime) {
                    validFromDateTime.formatDayMonthTime(context)
                } else {
                    validFromDateTime.toLocalDate().formatDayMonthYear()
                }
                textView.text = context.getString(
                    R.string.qr_card_validity_future_from, validFrom, if (showUntil) {
                        val until =
                            originState.origin.expirationTime.toLocalDate().formatDayMonthYear()
                        context.getString(R.string.qr_card_validity_future_until, until)
                    } else {
                        ""
                    }
                )

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

        descriptionLayout.addView(
            textView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(
                    0, 0, 0,
                    context.resources.getDimensionPixelSize(R.dimen.green_card_item_proof_spacing)
                )
            }
        )
    }
}