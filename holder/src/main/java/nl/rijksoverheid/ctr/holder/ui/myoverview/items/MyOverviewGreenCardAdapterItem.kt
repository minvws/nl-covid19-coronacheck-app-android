/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.content.Context
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.*
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewGreenCardBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.CredentialEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginEntity
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteCredentials
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class MyOverviewGreenCardAdapterItem(
    private val greenCard: GreenCard,
    private val sortedOrigins: List<OriginEntity>,
    private val credential: CredentialEntity?,
    private val onButtonClick: (qrCodeContent: ByteArray) -> Unit,
) :
    BindableItem<ItemMyOverviewGreenCardBinding>(R.layout.item_my_overview_green_card.toLong()),
    KoinComponent {

    private val testResultAdapterItemUtil: TestResultAdapterItemUtil by inject()

    override fun bind(viewBinding: ItemMyOverviewGreenCardBinding, position: Int) {
        applyStyling(
            viewBinding = viewBinding
        )

        setContent(
            viewBinding = viewBinding
        )

        viewBinding.button.setOnClickListener {
            credential?.let {
                onButtonClick.invoke(it.data)
            }
        }
    }

    private fun applyStyling(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context
        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_european_title)
                    setTextColor(ContextCompat.getColor(context, R.color.darkened_blue))
                }
                viewBinding.button.setButtonColor(R.color.darkened_blue)
            }
            is GreenCardType.Domestic -> {
                viewBinding.typeTitle.apply {
                    text = context.getString(R.string.validity_type_dutch_title)
                    setTextColor(ContextCompat.getColor(context, R.color.primary_blue))
                }
                viewBinding.button.setButtonColor(R.color.primary_blue)
            }
        }

        // Check enabling button
        viewBinding.button.isEnabled = credential != null
    }

    private fun setContent(viewBinding: ItemMyOverviewGreenCardBinding) {
        val context = viewBinding.root.context

        viewBinding.proof1Title.visibility = View.GONE
        viewBinding.proof1Subtitle.visibility = View.GONE
        viewBinding.proof2Title.visibility = View.GONE
        viewBinding.proof2Subtitle.visibility = View.GONE
        viewBinding.proof3Title.visibility = View.GONE
        viewBinding.proof3Subtitle.visibility = View.GONE
        viewBinding.proof1Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof2Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))
        viewBinding.proof3Subtitle.setTextColor(context.getThemeColor(android.R.attr.textColorPrimary))

        when (greenCard.greenCardEntity.type) {
            is GreenCardType.Eu -> {
                // European card only has one origin
                val origin = greenCard.origins.first()
                when (origin.type) {
                    is OriginType.Test -> {
                        viewBinding.proof1Title.setText(R.string.qr_card_test_title_eu)
                        viewBinding.proof1Subtitle.text = origin.expirationTime.formatDateTime(context)
                    }
                    is OriginType.Vaccination -> {
                        viewBinding.proof1Title.setText(R.string.qr_card_vaccination_title_eu)
                        viewBinding.proof1Subtitle.text = origin.eventTime.toLocalDate().formatDayMonthYear()
                    }
                    is OriginType.Recovery -> {
                        viewBinding.proof1Title.setText(R.string.qr_card_recovery_title_eu)
                        viewBinding.proof1Subtitle.text = origin.expirationTime.toLocalDate().formatDayMonth()
                    }
                }
                viewBinding.proof1Title.visibility = View.VISIBLE
                viewBinding.proof1Subtitle.visibility = View.VISIBLE
            }
            is GreenCardType.Domestic -> {
                // Domestic cards can have multiple origins
                sortedOrigins.forEach { origin ->
                    when (origin.type) {
                        is OriginType.Test -> {
                            viewBinding.proof1Title.setText(R.string.qr_card_test_domestic)
                            viewBinding.proof1Subtitle.text = context.getString(
                                R.string.qr_card_validity_valid,
                                origin.expirationTime.formatDateTime(context)
                            )

                            viewBinding.proof1Title.visibility = View.VISIBLE
                            viewBinding.proof1Subtitle.visibility = View.VISIBLE
                        }
                        is OriginType.Vaccination -> {
                            viewBinding.proof2Title.setText(R.string.qr_card_vaccination_title_domestic)

                            if (origin.validFrom.isAfter(OffsetDateTime.now())) {
                                viewBinding.proof2Subtitle.setTextColor(ContextCompat.getColor(context, R.color.link))

                                val hoursBetweenExpiration =
                                    ChronoUnit.HOURS.between(OffsetDateTime.now(), origin.validFrom)

                                if (hoursBetweenExpiration >= 24) {
                                    viewBinding.proof2Subtitle.text = context.getString(R.string.qr_card_validity_future_days,
                                        ChronoUnit.DAYS.between(OffsetDateTime.now(), origin.validFrom).coerceAtLeast(1).toString())
                                } else {
                                    viewBinding.proof2Subtitle.text = context.getString(R.string.qr_card_validity_future_hours,
                                       hoursBetweenExpiration.coerceAtLeast(1).toString())
                                }
                            } else {
                                viewBinding.proof2Subtitle.text = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.toLocalDate().formatDayMonthYear()
                                )
                            }

                            viewBinding.proof2Title.visibility = View.VISIBLE
                            viewBinding.proof2Subtitle.visibility = View.VISIBLE
                        }
                        is OriginType.Recovery -> {
                            viewBinding.proof3Title.setText(R.string.qr_card_recovery_title_domestic)

                            if (origin.validFrom.isAfter(OffsetDateTime.now())) {
                                viewBinding.proof3Subtitle.setTextColor(ContextCompat.getColor(context, R.color.link))

                                val hoursBetweenExpiration =
                                    ChronoUnit.HOURS.between(OffsetDateTime.now(), origin.validFrom)

                                if (hoursBetweenExpiration >= 24) {
                                    viewBinding.proof3Subtitle.text = context.getString(R.string.qr_card_validity_future_days,
                                        ChronoUnit.DAYS.between(OffsetDateTime.now(), origin.validFrom).coerceAtLeast(1).toString())
                                } else {
                                    viewBinding.proof3Subtitle.text = context.getString(R.string.qr_card_validity_future_hours,
                                        hoursBetweenExpiration.coerceAtLeast(1).toString())
                                }
                            } else {
                                viewBinding.proof3Subtitle.text = context.getString(
                                    R.string.qr_card_validity_valid,
                                    origin.expirationTime.toLocalDate().formatDayMonth()
                                )
                            }

                            viewBinding.proof3Title.visibility = View.VISIBLE
                            viewBinding.proof3Subtitle.visibility = View.VISIBLE
                        }
                    }
                }

                // If there is only one origin we can show a countdown if required
                if (sortedOrigins.size == 1) {
                    when (val expireCountDownResult =
                        testResultAdapterItemUtil.getExpireCountdownText(expireDate = sortedOrigins.first().expirationTime)) {
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
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_green_card
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewGreenCardBinding {
        return ItemMyOverviewGreenCardBinding.bind(view)
    }
}
