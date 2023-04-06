/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard.items

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import java.time.Clock
import java.time.Instant
import java.util.concurrent.TimeUnit
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthTime
import nl.rijksoverheid.ctr.design.ext.formatDayMonthYear
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.util.CredentialUtil
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.ext.capitalize
import nl.rijksoverheid.ctr.shared.ext.locale
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface DashboardGreenCardAdapterItemUtil {
    fun setContent(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        cards: List<AdapterCard>
    )
}

class DashboardGreenCardAdapterItemUtilImpl(
    private val utcClock: Clock,
    private val context: Context,
    private val credentialUtil: CredentialUtil,
    private val dashboardGreenCardAdapterItemExpiryUtil: DashboardGreenCardAdapterItemExpiryUtil
) : DashboardGreenCardAdapterItemUtil, KoinComponent {

    private val featureFlagUseCase: HolderFeatureFlagUseCase by inject()

    override fun setContent(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        cards: List<AdapterCard>
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
                            when (featureFlagUseCase.getDisclosurePolicy()) {
                                is DisclosurePolicy.ZeroG -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_testcertificate_0G).capitalize()
                                }
                                else -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_testcertificate).capitalize()
                                }
                            }
                            setEuTestOrigin(
                                dashboardGreenCardAdapterItemBinding, it, originState, origin
                            )
                            setExpiryText(origin, dashboardGreenCardAdapterItemBinding)
                        }
                        is OriginType.Vaccination -> {
                            when (featureFlagUseCase.getDisclosurePolicy()) {
                                is DisclosurePolicy.ZeroG -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_vaccinationcertificate_0G).capitalize()
                                }
                                else -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_vaccinationcertificate).capitalize()
                                }
                            }
                            setEuVaccinationOrigin(
                                dashboardGreenCardAdapterItemBinding, it, origin
                            )
                            setExpiryText(origin, dashboardGreenCardAdapterItemBinding)
                        }
                        is OriginType.Recovery -> {
                            when (featureFlagUseCase.getDisclosurePolicy()) {
                                is DisclosurePolicy.ZeroG -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_recoverycertificate_0G).capitalize()
                                }
                                else -> {
                                    dashboardGreenCardAdapterItemBinding.title.text =
                                        context.getString(R.string.general_recoverycertificate).capitalize()
                                }
                            }
                            setEuRecoveryOrigin(dashboardGreenCardAdapterItemBinding, originState, origin)
                            setExpiryText(origin, dashboardGreenCardAdapterItemBinding)
                        }
                        is OriginType.VaccinationAssessment -> {
                            // Visitor pass is only for domestic
                        }
                    }
                }
            }
        }

        val originStates = cards.first().originStates
        val becomesValidAutomatically =
            originStates.size == 1 && originStates.first() is OriginState.Future
        if (becomesValidAutomatically) {
            dashboardGreenCardAdapterItemBinding.expiresIn.visibility = View.VISIBLE
            dashboardGreenCardAdapterItemBinding.expiresIn.text = context.getString(R.string.qr_card_validity_future)
        }
    }

    private fun setDomesticTestOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            title = context.getString(R.string.qr_card_test_domestic)
        )

        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            originState = originState,
            showTime = false,
            subtitle = context.getString(R.string.qr_card_validity_valid,
                origin.expirationTime.formatDateTime(context)
            )
        )
    }

    private fun setDomesticVaccinationOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        if (origin.doseNumber == null) {
            setOriginTitle(
                descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
                title = context.getString(R.string.qr_card_vaccination_title_domestic)
            )
        } else {
            when (origin.doseNumber) {
                1 -> {
                    setOriginTitle(
                        descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
                        title = context.getString(
                            R.string.qr_card_vaccination_title_domestic_with_dosis,
                            origin.doseNumber.toString()
                        )
                    )
                }
                else -> {
                    setOriginTitle(
                        descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
                        title = context.getString(
                            R.string.qr_card_vaccination_title_domestic_with_doses,
                            origin.doseNumber.toString()
                        )
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
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
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
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            title = context.getString(R.string.qr_card_recovery_title_domestic)
        )

        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            originState = originState,
            showTime = true,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayMonthYear()
            )
        )
    }

    private fun setDomesticVaccinationAssessmentOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            title = "${context.getString(R.string.general_visitorPass).capitalize()}:"
        )

        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            originState = originState,
            showTime = false,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.formatDateTime(context)
            )
        )
    }

    private fun setEuRecoveryOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        originState: OriginState,
        origin: OriginEntity
    ) {
        // EU recovery description has no title so we put only the space in between for correct alignment
        dashboardGreenCardAdapterItemBinding.description.addView(
            Space(context), LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                context.resources.getDimensionPixelSize(R.dimen.green_card_item_proof_spacing)
            )
        )
        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            originState = originState,
            showTime = true,
            subtitle = context.getString(
                R.string.qr_card_validity_valid,
                origin.expirationTime.toLocalDate().formatDayMonthYear()
            )
        )
    }

    private fun setEuVaccinationOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        greenCard: GreenCard,
        origin: OriginEntity
    ) {
        val getCurrentDosesString: (String, String, String) -> String =
            { currentDose: String, sumDoses: String, country: String ->
                val dosisString = context.getString(
                    R.string.qr_card_vaccination_doses,
                    currentDose, sumDoses
                )
                if (country.isNotEmpty()) {
                    "$dosisString$country"
                } else {
                    dosisString
                }
            }
        val doses = credentialUtil.getVaccinationDosesCountryLineForEuropeanCredentials(
            greenCard.credentialEntities,
            context.locale().language,
            getCurrentDosesString
        )
        setOriginTitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            title = doses
        )

        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            // force a valid origin, as we need to allow the user to view the QR
            // and when is valid from, it depends from the country going to
            originState = OriginState.Valid(greenCard.origins.first()),
            showTime = false,
            subtitle = "${context.getString(R.string.qr_card_vaccination_title_eu)} ${
                origin.eventTime.toLocalDate().formatDayMonthYear()
            }"
        )
    }

    private fun setEuTestOrigin(
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper,
        greenCard: GreenCard,
        originState: OriginState,
        origin: OriginEntity
    ) {
        setOriginTitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            title = "${context.getString(R.string.qr_card_test_eu)} ${
                credentialUtil.getTestTypeForEuropeanCredentials(
                    greenCard.credentialEntities
                )
            }"
        )

        setOriginSubtitle(
            descriptionLayout = dashboardGreenCardAdapterItemBinding.description,
            originState = originState,
            showTime = false,
            subtitle = "${context.getString(R.string.qr_card_test_title_eu)} ${
                origin.eventTime.formatDateTime(
                    context
                )
            }"
        )
    }

    private fun setOriginTitle(
        descriptionLayout: LinearLayout,
        title: String
    ) {
        descriptionLayout.addView(
            TextView(descriptionLayout.context).apply {
                setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
                text = title
                setTextIsSelectable(true)
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
        subtitle: String
    ) {
        val textView = TextView(descriptionLayout.context).apply {
            setTextAppearance(R.style.App_TextAppearance_MaterialComponents_Body1)
            setTextIsSelectable(true)
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
        dashboardGreenCardAdapterItemBinding: DashboardGreenCardAdapterItemBindingWrapper
    ) {
        val expireCountDownResult = dashboardGreenCardAdapterItemExpiryUtil.getExpireCountdown(
            origin.expirationTime, origin.type
        )
        if (expireCountDownResult is DashboardGreenCardAdapterItemExpiryUtil.ExpireCountDown.Show) {
            dashboardGreenCardAdapterItemBinding.expiresIn.visibility = View.VISIBLE
            dashboardGreenCardAdapterItemBinding.expiresIn.text =
                dashboardGreenCardAdapterItemExpiryUtil.getExpiryText(
                    expireCountDownResult
                )
        } else {
            dashboardGreenCardAdapterItemBinding.expiresIn.visibility = View.GONE
        }
    }
}
