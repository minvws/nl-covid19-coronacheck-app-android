/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.create_qr

import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.adevinta.android.barista.assertion.BaristaEnabledAssertions.assertEnabled
import com.adevinta.android.barista.assertion.BaristaErrorAssertions.assertErrorDisplayed
import com.adevinta.android.barista.assertion.BaristaHintAssertions.assertHint
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.adevinta.android.barista.interaction.BaristaEditTextInteractions
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.TestResult
import nl.rijksoverheid.ctr.shared.livedata.Event
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InputTokenFragmentTest : AutoCloseKoinTest() {

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_input_token)
    }

    @Test
    fun `Correct default values shown when entering without deeplink`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}

        launchInputFragment(inputTokenViewModel = inputTokenViewModel)
        assertDisplayed(R.id.description, R.string.commercial_test_code_description)
        assertDisplayed(R.id.unique_code_input)
        assertHint(R.id.unique_code_input, R.string.commercial_test_unique_code_header)
        assertEnabled(R.id.button)
        assertNotDisplayed(R.id.verification_code_input)
    }

    @Test
    fun `Error code is shown when unique code is empty`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}
        (inputTokenViewModel.testResult as MutableLiveData).postValue(Event(TestResult.EmptyToken))
        launchInputFragment(inputTokenViewModel = inputTokenViewModel)

        assertErrorDisplayed(R.id.unique_code_input, R.string.commercial_test_error_empty_retrieval_code)
    }

    @Test
    fun `Error code is shown when unique code is invalid`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}
        (inputTokenViewModel.testResult as MutableLiveData).postValue(Event(TestResult.InvalidToken))
        launchInputFragment(inputTokenViewModel = inputTokenViewModel)

        assertErrorDisplayed(R.id.unique_code_input, R.string.commercial_test_error_invalid_code)
    }

    @Test
    fun `Verification code is shown when verification required`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}
        (inputTokenViewModel.viewState as MutableLiveData).postValue(ViewState(verificationRequired = true))
        launchInputFragment(inputTokenViewModel = inputTokenViewModel)

        assertDisplayed(R.id.verification_code_input)
    }

    @Test
    fun `Verification code error is shown when verification empty`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}
        (inputTokenViewModel.testResult as MutableLiveData).postValue(Event(TestResult.EmptyVerificationCode))
        (inputTokenViewModel.viewState as MutableLiveData).postValue(ViewState(verificationRequired = true))
        launchInputFragment(inputTokenViewModel = inputTokenViewModel)

        assertErrorDisplayed(R.id.verification_code_input, R.string.commercial_test_error_empty_verification_code)
    }

    @Test
    fun `Verification code error is shown when invalid verification code`() {
        val inputTokenViewModel = object: FakeInputTokenViewModel() {}
        (inputTokenViewModel.testResult as MutableLiveData).postValue(Event(TestResult.VerificationRequired))
        (inputTokenViewModel.viewState as MutableLiveData).postValue(ViewState(verificationRequired = true))
        launchInputFragment(inputTokenViewModel = inputTokenViewModel)
        BaristaEditTextInteractions.writeTo(R.id.verification_code_text, "123")
        inputTokenViewModel.testResult.postValue(Event(TestResult.VerificationRequired))

        assertErrorDisplayed(R.id.verification_code_input, R.string.commercial_test_error_invalid_combination)
    }

    private fun launchInputFragment(
        token: String? = null,
        inputTokenViewModel: InputTokenViewModel) {
        loadKoinModules(
            module(override = true) {
                viewModel {
                    inputTokenViewModel
                }
            }
        )

        launchFragmentInContainer(
            // Supply navArgs
            bundleOf(
                "token" to token,
                "toolbarTitle" to "",
                "data" to InputTokenFragmentData.CommercialTest,
            ), themeResId = R.style.AppTheme
        ) {
            InputTokenFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }
    }
}

abstract class FakeInputTokenViewModel: InputTokenViewModel() {
    override fun updateViewState() {

    }

    override fun getTestResult(fromDeeplink: Boolean) {

    }

    override fun sendVerificationCode() {

    }
}