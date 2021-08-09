package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelStore
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertContains
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.interaction.BaristaDialogInteractions.clickDialogPositiveButton
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.design.utils.DialogUtilImpl
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.fakeCommercialTestResultViewModel
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.holder.ui.create_qr.CommercialTestCodeFragment
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import nl.rijksoverheid.ctr.shared.livedata.Event
import org.junit.Rule

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
class MyOverviewFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val dialogUtil: DialogUtil = mockk(relaxed = true)

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_my_overview)
    }

    @Test
    fun `network error triggers dialog with right copy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = launchFragment()
        triggerNetworkError(viewModel)

        verify { dialogUtil.presentDialog(
            context = any(),
            title = R.string.dialog_title_no_internet,
            message = context.getString(R.string.dialog_credentials_expired_no_internet),
            positiveButtonText = R.string.app_status_internet_required_action,
            positiveButtonCallback = any(),
            negativeButtonText = R.string.dialog_close,
        ) }
    }

    private fun triggerNetworkError(viewModel: MyOverviewViewModel) = triggerSyncerResult(viewModel, DatabaseSyncerResult.NetworkError(true))

    private fun triggerSyncerResult(viewModel: MyOverviewViewModel, syncResult: DatabaseSyncerResult) {
        ((viewModel.databaseSyncerResultLiveData) as MutableLiveData).postValue(Event(
            syncResult))
    }

    private fun launchFragment(selectType: GreenCardType = GreenCardType.Domestic): MyOverviewViewModel {
        val viewModel = object: MyOverviewViewModel() {
            override fun getSelectedType(): GreenCardType {
                return selectType
            }

            override fun refreshOverviewItems(
                selectType: GreenCardType,
                forceSync: Boolean
            ) {
                //
            }
        }
        loadKoinModules(
            module(override = true) {
                viewModel<MyOverviewViewModel> { viewModel }

                factory<DialogUtil> { dialogUtil }
                factory<CachedAppConfigUseCase> { object: CachedAppConfigUseCase {
                    override fun getCachedAppConfig(): HolderConfig {
                        return HolderConfig.default()
                    }

                    override fun getProviderName(providerIdentifier: String): String {
                        return "GGD"
                    }
                } }
            }
        )

        launchFragmentInContainer(
            // Supply navArgs
            bundleOf(
                "returnUri" to "",
                MyOverviewFragment.GREEN_CARD_TYPE to GreenCardType.Domestic,
            ), themeResId = R.style.AppTheme
        ) {
            MyOverviewFragment().also {
                it.viewLifecycleOwnerLiveData.observeForever { viewLifecycleOwner ->
                    if (viewLifecycleOwner != null) {
                        Navigation.setViewNavController(it.requireView(), navController)
                    }
                }
            }
        }

        return viewModel
    }
}