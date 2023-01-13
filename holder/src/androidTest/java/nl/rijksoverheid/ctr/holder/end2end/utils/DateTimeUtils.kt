/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

// Based on https://github.com/AdevintaSpain/Barista/issues/324#issuecomment-692019057

package nl.rijksoverheid.ctr.holder.end2end.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiCollection
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import timber.log.Timber

class DateTimeUtils(private val device: UiDevice) {

    fun setDate(date: LocalDate) {
        Timber.tag("end2end").d("Setting the device date to ${date.short()}")
        openDateSettings()
        toggleAutomaticDateTime(false)
        setDeviceDate(date)
    }

    fun resetDateToAutomatic() {
        Timber.tag("end2end").d("Resetting the device date to automatic")
        openDateSettings()
        toggleAutomaticDateTime(true)
    }

    private fun openDateSettings() {
        device.pressHome()

        val launcherPackage = device.launcherPackageName!!
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), 5000)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(Settings.ACTION_DATE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @Throws(UiObjectNotFoundException::class)
    private fun toggleAutomaticDateTime(enable: Boolean) {
        val dateTimeSettingList = UiScrollable(UiSelector().className(recyclerViewClassName))
        val automaticDateTimeOption = dateTimeSettingList.getChildByText(UiSelector().className(linearLayoutClassName), dateSwitchLabel)
        val automaticDateTimeSwitch = automaticDateTimeOption.getChild(UiSelector().className(switchClassName))

        if (automaticDateTimeSwitch.isChecked == enable) return
        automaticDateTimeSwitch.click()
    }

    private fun setDeviceDate(targetDate: LocalDate) {
        clickSetDateButton()
        val currentDate = getCurrentDeviceDate()
        selectTargetMonth(currentDate, targetDate)
        selectTargetDay(targetDate)
        clickOkButton()
    }

    private fun clickSetDateButton() {
        val dateTimeSettingList = UiScrollable(UiSelector().className(recyclerViewClassName))
        val setDate = dateTimeSettingList.getChildByText(UiSelector().className(textViewClassName), dateLabel)
        setDate.click()
    }

    private fun getCurrentDeviceDate(): LocalDate {
        val yearHeader = findObjectByResourceName(datePickerHeaderYearResource)
        val dateHeader = findObjectByResourceName(datePickerHeaderDateResource)

        val year = yearHeader.text
        val dateWithoutYear = dateHeader.text.lowercase()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("E d MMM yyyy").withLocale(java.util.Locale("nl", "NL"))
        val date = LocalDate.parse("$dateWithoutYear $year", dateTimeFormatter)
        Timber.tag("end2end").d("Current device date is $date")
        return date
    }

    private fun selectTargetMonth(currentDate: LocalDate, targetDate: LocalDate) {
        val monthDiff = ChronoUnit.MONTHS.between(currentDate.withDayOfMonth(1), targetDate.withDayOfMonth(1))
            Timber.tag("end2end").d("Months to shift: $monthDiff")
        when {
            monthDiff == 0L -> {
                // ...nothing to do. Currently on the right month.
            }
            monthDiff < 0L -> {
                for (i in monthDiff until 0) {
                    val previousMonthButtonItem = findObjectByResourceName(prevMonthResource)
                    previousMonthButtonItem.click()
                }
            }
            else -> {
                for (i in 0 until monthDiff) {
                    val nextMonthButtonItem = findObjectByResourceName(nextMonthResource)
                    nextMonthButtonItem.click()
                }
            }
        }
    }

    private fun selectTargetDay(targetDate: LocalDate) {
        Timber.tag("end2end").d("Target day: ${targetDate.dayOfMonth}")
        val dayList = UiCollection(UiSelector().resourceId(monthViewResource))
        val dayItem = dayList.getChild(UiSelector().index(targetDate.dayOfMonth - 1))
        dayItem.click()
    }

    private fun clickOkButton() {
        device.findObject(By.text("OK")).click()
    }

    private fun findObjectByResourceName(resourceName: String) =
        device.findObject(UiSelector().resourceId(resourceName))

    private val recyclerViewClassName = RecyclerView::class.java.canonicalName!!
    private val linearLayoutClassName = LinearLayout::class.java.canonicalName!!
    private val switchClassName = Switch::class.java.canonicalName!!
    private val textViewClassName = TextView::class.java.canonicalName!!

    companion object {
        private const val dateSwitchLabel = "Tijd automatisch instellen"
        private const val dateLabel = "Datum"
        private const val datePickerHeaderYearResource = "android:id/date_picker_header_year"
        private const val datePickerHeaderDateResource = "android:id/date_picker_header_date"
        private const val nextMonthResource = "android:id/next"
        private const val prevMonthResource = "android:id/prev"
        private const val monthViewResource = "android:id/month_view"
    }
}
