/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import junit.framework.Assert.assertEquals
import nl.rijksoverheid.ctr.design.ext.formatDateTime
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeTestResultsViewModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.Holder
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.SignedTestResult
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.models.PersonalDetails
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowAlertDialog
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertNotNull


@RunWith(RobolectricTestRunner::class)
class YourNegativeTestResultFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_your_negative_result)
    }

    @Test
    fun `Return to overview if returning to state with no test result`() {
        launchNegativeTestResultFragment(null, null)
        assertEquals(navController.currentDestination?.id, R.id.nav_my_overview)
    }

    @Test
    fun `Test results are shown if result is provided`() {
        val negativeTestResult = getNegativeRetrievedTestResult()
        launchNegativeTestResultFragment(
            retrievedTestResult = negativeTestResult, fakeSignedTestResult = null
        )
        // Check if group is shown
        assertDisplayed(R.id.test_results_group)

        // Check if correct date is shown
        assertContains(
            R.id.row_subtitle, OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(negativeTestResult.remoteTestResult.result!!.sampleDate.toEpochSecond()),
                ZoneOffset.UTC
            ).formatDateTime(ApplicationProvider.getApplicationContext())
        )
        // Check if personal details are shown
        assertContains(
            R.id.row_personal_details,
            "${negativeTestResult.personalDetails.firstNameInitial} ${negativeTestResult.personalDetails.lastNameInitial} ${negativeTestResult.personalDetails.birthDay} ${negativeTestResult.personalDetails.birthMonth}"
        )
    }

    @Test
    fun `Show 'already signed' screen if test result is already signed`() {
        val negativeTestResult = getNegativeRetrievedTestResult()
        launchNegativeTestResultFragment(
            retrievedTestResult = negativeTestResult,
            fakeSignedTestResult = SignedTestResult.AlreadySigned
        )
        // Click save button
        clickOn(R.id.bottom)
        assertEquals(navController.currentDestination?.id, R.id.nav_no_test_result)
    }

    @Test
    fun `Show network error popup if network error occurs`() {
        val negativeTestResult = getNegativeRetrievedTestResult()
        launchNegativeTestResultFragment(
            retrievedTestResult = negativeTestResult,
            fakeSignedTestResult = SignedTestResult.NetworkError
        )

        // Click save button
        clickOn(R.id.bottom)

        assertNotNull(ShadowAlertDialog.getLatestDialog())
    }


    private fun getNegativeRetrievedTestResult(): TestResult.NegativeTestResult {
        return TestResult.NegativeTestResult(
            remoteTestResult = RemoteTestResult(
                result = RemoteTestResult.Result(
                    unique = "12345",
                    sampleDate = OffsetDateTime.now(),
                    testType = "PCR",
                    negativeResult = true,
                    holder = Holder(
                        firstNameInitial = "X",
                        lastNameInitial = "Y", birthDay = "1", birthMonth = "1"
                    )
                ),
                protocolVersion = "1",
                providerIdentifier = "1",
                status = RemoteTestResult.Status.COMPLETE
            ),
            personalDetails = PersonalDetails(
                firstNameInitial = "X",
                lastNameInitial = "Y", birthDay = "1", birthMonth = "1"
            ),
            signedResponseWithTestResult = SignedResponseWithModel(
                rawResponse = "dummy".toByteArray(),
                model = RemoteTestResult(
                    result = null,
                    protocolVersion = "1",
                    providerIdentifier = "1",
                    status = RemoteTestResult.Status.COMPLETE
                ),
            )
        )
    }


    private fun launchNegativeTestResultFragment(
        retrievedTestResult: TestResult.NegativeTestResult? = null,
        fakeSignedTestResult: SignedTestResult? = null
    ) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    fakeTestResultsViewModel(retrievedTestResult, fakeSignedTestResult)
                }
            }
        )
        launchFragmentInContainer(
            themeResId = R.style.AppTheme
        ) {
            YourNegativeTestResultFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}
