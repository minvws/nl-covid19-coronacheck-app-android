/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.end2end

import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.time.LocalDate
import nl.rijksoverheid.ctr.appconfig.persistence.AppUpdatePersistenceManager
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.end2end.utils.overrideModules
import nl.rijksoverheid.ctr.introduction.persistance.IntroductionPersistenceManager
import nl.rijksoverheid.ctr.persistence.PersistenceManager
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy
import org.junit.Before
import org.koin.test.AutoCloseKoinTest
import org.koin.test.inject

abstract class BaseTest : AutoCloseKoinTest() {

    private val persistenceManager: PersistenceManager by inject()
    private val introductionPersistenceManager: IntroductionPersistenceManager by inject()
    private val appUpdatePersistenceManager: AppUpdatePersistenceManager by inject()

    private lateinit var scenario: ActivityScenario<HolderMainActivity>

    fun relaunchApp() {
        ActivityScenario.launch(HolderMainActivity::class.java)
        instrumentation.waitForIdleSync()
    }

    @Before
    fun startApp() {
        overrideModules(listOf())

        persistenceManager.setHasDismissedUnsecureDeviceDialog(true)
        persistenceManager.setHasDismissedRootedDeviceDialog()

        introductionPersistenceManager.saveIntroductionFinished()
        appUpdatePersistenceManager.saveNewFeaturesSeen(3)
        appUpdatePersistenceManager.saveNewTermsSeen(2)
        persistenceManager.setPolicyScreenSeen(DisclosurePolicy.ZeroG)
        persistenceManager.setSelectedDashboardTab(1)

        scenario = ActivityScenario.launch(HolderMainActivity::class.java)
    }

    companion object {
        val instrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
        val device: UiDevice = UiDevice.getInstance(instrumentation)
        val today: LocalDate = LocalDate.now()
        val authPassword: String =
            InstrumentationRegistry.getArguments().getString("authPassword", "")
    }
}
