package nl.rijksoverheid.ctr.holder.ui.myoverview.items

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.design.ext.getAppThemeWindowBackgroundColor
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.ItemMyOverviewTestResultBinding
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.ui.myoverview.utils.TestResultAdapterItemUtil
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewTestResultAdapterItem(
    private val localTestResult: LocalTestResult,
    private val onButtonClick: () -> Unit,
) :
    BindableItem<ItemMyOverviewTestResultBinding>(R.layout.item_my_overview_test_result.toLong()),
    KoinComponent {

    private val testResultAdapterItemUtil: TestResultAdapterItemUtil by inject()

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: ItemMyOverviewTestResultBinding, position: Int) {
        val context = viewBinding.root.context
        val backgroundColor = context.getAppThemeWindowBackgroundColor()

        viewBinding.gradient.background = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                backgroundColor,
                backgroundColor,
                backgroundColor,
                Color.parseColor("#00FFFFFF")
            )
        )

        val personalDetails = localTestResult.personalDetails

        viewBinding.personalDetails.text =
            "${personalDetails.firstNameInitial} ${personalDetails.lastNameInitial} ${personalDetails.birthDay} ${personalDetails.birthMonth}"
        viewBinding.personalDetails.contentDescription = context.getString(
            R.string.accessibility_label_my_overview_test_result_personal_data,
            personalDetails.firstNameInitial,
            personalDetails.lastNameInitial,
            personalDetails.birthDay,
            personalDetails.birthMonth
        )

        viewBinding.validity.text = context.getString(
            R.string.my_overview_test_result_validity,
            localTestResult.expireDate.formatDateTime(context)
        )

        // Handle count
        when (val expireCountDownResult =
            testResultAdapterItemUtil.getExpireCountdownText(expireDate = localTestResult.expireDate)) {
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

        viewBinding.button.setOnClickListener {
            onButtonClick.invoke()
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_my_overview_test_result
    }

    override fun initializeViewBinding(view: View): ItemMyOverviewTestResultBinding {
        return ItemMyOverviewTestResultBinding.bind(view)
    }
}
