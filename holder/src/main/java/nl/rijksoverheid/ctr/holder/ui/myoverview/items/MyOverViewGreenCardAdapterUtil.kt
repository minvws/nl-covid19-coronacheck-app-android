package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import mobilecore.Mobilecore
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.design.ext.formatDayShortMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.MyOverviewGreenCardExpiryUtil
import nl.rijksoverheid.ctr.shared.ext.capitalize
import java.time.Clock
import java.time.Instant
import java.util.concurrent.TimeUnit

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
    private val utcClock: Clock,
    private val context: Context,
    private val credentialUtil: CredentialUtil,
    private val myOverviewGreenCardExpiryUtil: MyOverviewGreenCardExpiryUtil,
    private val featureFlagUseCase: FeatureFlagUseCase
) : MyOverViewGreenCardAdapterUtil {

    override fun setContent(
        viewBinding: ViewBindingWrapper,
        cards: List<AdapterCard>,
    ) {
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
                                context.getString(R.string.general_testcertificate).capitalize()
                            setEuTestOrigin(
                                viewBinding, it, originState, origin
                            )
                        }
                        is OriginType.Vaccination -> {
                            viewBinding.title.text =
                                context.getString(R.string.general_vaccinationcertificate).capitalize()
                            setEuVaccinationOrigin(
                                viewBinding, it, origin
                            )
                        }
                        is OriginType.Recovery -> {
                            viewBinding.title.text =
                                context.getString(R.string.general_recoverycertificate).capitalize()
                            setEuRecoveryOrigin(viewBinding, originState, origin)
                        }
                        is OriginType.VaccinationAssessment -> {
                            // Visitor pass is only for domestic
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
                                    viewBinding, originState, origin
                                )
                                is OriginType.Recovery -> setDomesticRecoveryOrigin(
                                    viewBinding, originState, origin
                                )
                                is OriginType.Test -> setDomesticTestOrigin(
                                    viewBinding, originState, origin, it.credentialEntities
                                )
                                is OriginType.VaccinationAssessment -> setDomesticVaccinationAssessmentOrigin(
                                    viewBinding, originState, origin
                                )
                            }
                        }
                    // When there is only 1 valid origin set potential expiry countdown
                    myOverviewGreenCardExpiryUtil.getLastValidOrigin(it.origins)?.let {
                        setExpiryText(it, viewBinding)
                    }
                }
            }
        }

        val originStates = cards.first().originStates
        val becomesValidAutomatically =
            originStates.size == 1 && originStates.first() is OriginState.Future
        if (becomesValidAutomatically) {
            viewBinding.expiresIn.visibility = View.VISIBLE
            viewBinding.expiresIn.text = context.getString(R.string.qr_card_validity_future)
        }
    }

    private fun setDomesticTestOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        origin: OriginEntity,
        credentialEntities: List<CredentialEntity>
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = context.getString(R.string.qr_card_test_domestic),
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = false,
            subtitle = context.getString(
                if (credentialEntities.any { it.category == Mobilecore.VERIFICATION_POLICY_3G } && featureFlagUseCase.isVerificationPolicyEnabled()) {
                    R.string.holder_my_overview_test_result_validity_3g
                } else {
                    R.string.qr_card_validity_valid
                },
                origin.expirationTime.formatDateTime(context)
            )
        )
    }

    private fun setDomesticVaccinationOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        if (origin.doseNumber == null) {
            setOriginTitle(
                descriptionLayout = viewBinding.description,
                title = context.getString(R.string.qr_card_vaccination_title_domestic),
            )
        } else {
            when (origin.doseNumber) {
                1 -> {
                    setOriginTitle(
                        descriptionLayout = viewBinding.description,
                        title = context.getString(
                            R.string.qr_card_vaccination_title_domestic_with_dosis,
                            origin.doseNumber.toString()
                        ),
                    )
                }
                else -> {
                    setOriginTitle(
                        descriptionLayout = viewBinding.description,
                        title = context.getString(
                            R.string.qr_card_vaccination_title_domestic_with_doses,
                            origin.doseNumber.toString()
                        ),
                    )
                }
            }
        }


        val subtitle = if (originExpirationTimeThreeYearsFromNow(originState.origin)) {
            context.getString(
                R.string.qr_card_validity_future_from,
                origin.validFrom.toLocalDate().formatDayMonthYear(),
                ""
            )
        } else {
            context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayMonthYear()
            )
        }

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = true,
            subtitle = subtitle
        )
    }

    /**
     * Returns if the origin will expire in more than three years from now
     * @param origin The origin to check
     */
    private fun originExpirationTimeThreeYearsFromNow(origin: OriginEntity): Boolean {
        val expirationSecondsFromNow =
            origin.expirationTime.toInstant().epochSecond - Instant.now(utcClock).epochSecond
        val expirationYearsFromNow = TimeUnit.SECONDS.toDays(expirationSecondsFromNow) / 365
        return expirationYearsFromNow >= 3
    }

    private fun setDomesticRecoveryOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = context.getString(R.string.qr_card_recovery_title_domestic),
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = true,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayShortMonthYear()
            ),
        )
    }

    private fun setDomesticVaccinationAssessmentOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = "${context.getString(R.string.general_visitorPass).capitalize()}:"
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = false,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.formatDateTime(context)
            )
        )
    }

    private fun setEuRecoveryOrigin(
        viewBinding: ViewBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        // EU recovery description has no title so we put only the space in between for correct alignment
        viewBinding.description.addView(
            Space(context), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.resources.getDimensionPixelSize(R.dimen.green_card_item_proof_spacing)
            )
        )
        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = true,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayShortMonthYear()
            ),
        )
    }

    private fun setEuVaccinationOrigin(
        viewBinding: ViewBindingWrapper,
        greenCard: GreenCard,
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
            showTime = false,
            subtitle = "${context.getString(R.string.qr_card_vaccination_title_eu)} ${
                origin.eventTime.toLocalDate().formatDayMonthYear()
            }",
        )
    }

    private fun setEuTestOrigin(
        viewBinding: ViewBindingWrapper,
        greenCard: GreenCard,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = viewBinding.description,
            title = "${context.getString(R.string.qr_card_test_eu)} ${
                credentialUtil.getTestTypeForEuropeanCredentials(
                    greenCard.credentialEntities
                )
            }",
        )

        setOriginSubtitle(
            descriptionLayout = viewBinding.description,
            originState = originState,
            showTime = false,
            subtitle = "${context.getString(R.string.qr_card_test_title_eu)} ${
                origin.eventTime.formatDateTime(
                    context
                )
            }",
        )
    }

    private fun setOriginTitle(
        descriptionLayout: LinearLayout,
        title: String,
    ) {
        descriptionLayout.addView(
            TextView(descriptionLayout.context).apply {
                setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
                text = title
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                val topMargin =
                    context.resources.getDimensionPixelSize(R.dimen.green_card_item_proof_spacing)
                setMargins(0, topMargin, 0, 0)
            }
        )
    }

    private fun setOriginSubtitle(
        descriptionLayout: LinearLayout,
        originState: OriginState,
        showTime: Boolean,
        subtitle: String,
    ) {
        val textView = TextView(descriptionLayout.context).apply {
            setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
        }

        when (originState) {
            is OriginState.Future -> {
                val showUntil =
                    originState.origin.type == OriginType.Vaccination &&
                            !originExpirationTimeThreeYearsFromNow(originState.origin) ||
                            originState.origin.type == OriginType.Recovery

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

        descriptionLayout.addView(textView)
    }

    private fun setExpiryText(
        origin: OriginEntity,
        viewBinding: ViewBindingWrapper
    ) {
        val expireCountDownResult = myOverviewGreenCardExpiryUtil.getExpireCountdown(
            origin.expirationTime, origin.type
        )
        if (expireCountDownResult is MyOverviewGreenCardExpiryUtil.ExpireCountDown.Show) {
            viewBinding.expiresIn.visibility = View.VISIBLE
            viewBinding.expiresIn.text =
                myOverviewGreenCardExpiryUtil.getExpiryText(
                    expireCountDownResult
                )
        } else {
            viewBinding.expiresIn.visibility = View.GONE
        }
    }
}