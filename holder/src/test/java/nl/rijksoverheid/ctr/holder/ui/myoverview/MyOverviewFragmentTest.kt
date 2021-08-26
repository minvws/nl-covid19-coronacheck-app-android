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
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotExist
import io.mockk.mockk
import io.mockk.verify
import nl.rijksoverheid.ctr.appconfig.api.model.HolderConfig
import nl.rijksoverheid.ctr.design.utils.DialogUtil
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.persistence.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.holder.persistence.database.entities.*
import nl.rijksoverheid.ctr.holder.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItem
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.MyOverviewItems
import nl.rijksoverheid.ctr.holder.ui.create_qr.util.OriginState
import nl.rijksoverheid.ctr.shared.livedata.Event
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.Step
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.IllegalStateException
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "nl-land")
class MyOverviewFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val dialogUtil: DialogUtil = mockk(relaxed = true)

    private val eventTimeClock = Clock.fixed(Instant.parse("2021-06-01T00:00:00.00Z"), ZoneId.of("UTC"))
    private val expireTimeClock = Clock.fixed(Instant.parse("2021-07-01T00:00:00.00Z"), ZoneId.of("UTC"))

    private val navController = TestNavHostController(
        ApplicationProvider.getApplicationContext()
    ).also {
        it.setViewModelStore(ViewModelStore())
        it.setGraph(R.navigation.holder_nav_graph_main)
        it.setCurrentDestination(R.id.nav_my_overview)
    }

    @Test
    fun `network error with no credentials left triggers dialog with right copy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = launchFragment()

        triggerNetworkError(viewModel)

        verify {
            dialogUtil.presentDialog(
                context = any(),
                title = R.string.dialog_title_no_internet,
                message = context.getString(R.string.dialog_credentials_expired_no_internet),
                positiveButtonText = R.string.app_status_internet_required_action,
                positiveButtonCallback = any(),
                negativeButtonText = R.string.dialog_close,
            )
        }
    }

    @Test
    fun `network error with credentials left triggers dialog with right copy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = launchFragment()

        triggerNetworkError(viewModel, false)

        verify {
            dialogUtil.presentDialog(
                context = any(),
                title = R.string.dialog_title_no_internet,
                message = context.getString(R.string.dialog_update_credentials_no_internet),
                positiveButtonText = R.string.app_status_internet_required_action,
                positiveButtonCallback = any(),
                negativeButtonText = R.string.dialog_close,
            )
        }
    }

    @Test
    fun `overview with one green card item (vaccination) shows properly`() {
        val viewModel = launchFragment()

        (viewModel.myOverviewItemsLiveData as MutableLiveData).postValue(Event(
            myOverViewItemsWithValidDomesticGreenCard()
        ))

        assertDisplayed(R.id.type_title, R.string.validity_type_dutch_title)
        assertDisplayed(R.id.title, R.string.my_overview_test_result_title)
        assertDisplayed(R.id.proof1_title, R.string.qr_card_vaccination_title_domestic)
        assertDisplayed(R.id.proof1_subtitle, "geldig vanaf 1 juni 2021   ")
        assertNotDisplayed(R.id.proof2_title)
        assertNotDisplayed(R.id.proof3_title)
        assertNotDisplayed(R.id.proof2_subtitle)
        assertNotDisplayed(R.id.proof3_subtitle)
    }

    @Test
    fun `overview with one expired green card item shows properly`() {
        val viewModel = launchFragment()

        (viewModel.myOverviewItemsLiveData as MutableLiveData).postValue(Event(
            expiredItem()
        ))

        assertDisplayed(R.id.text, R.string.qr_card_expired)
        assertNotExist(R.id.test_result)
    }

    @Test
    fun `overview with one origin info item shows properly`() {
        val viewModel = launchFragment()

        (viewModel.myOverviewItemsLiveData as MutableLiveData).postValue(Event(
            originInfoItem()
        ))

        assertDisplayed(R.id.text, "Je vaccinatiebewijs is niet geldig in Nederland. Je hebt wel een internationaal bewijs.")
        assertNotExist(R.id.test_result)
    }

    @Test
    fun `overview with one clock deviation item shows properly`() {
        val viewModel = launchFragment()

        (viewModel.myOverviewItemsLiveData as MutableLiveData).postValue(Event(
            clockDeviationItem()
        ))

        assertDisplayed(R.id.text, R.string.my_overview_clock_deviation_description)
        assertNotExist(R.id.test_result)
    }

    private fun clockDeviationItem() = MyOverviewItems(
        items = listOf(
            MyOverviewItem.ClockDeviationItem
        ),
        selectedType = GreenCardType.Domestic
    )

    private fun originInfoItem() = MyOverviewItems(
        items = listOf(
            MyOverviewItem.OriginInfoItem(
                greenCardType = GreenCardType.Domestic,
                originType = OriginType.Vaccination,
            )
        ),
        selectedType = GreenCardType.Domestic
    )

    private fun expiredItem() = MyOverviewItems(
        items = listOf(
            MyOverviewItem.GreenCardExpiredItem(
                greenCardType = GreenCardType.Domestic
            )
        ),
        selectedType = GreenCardType.Domestic
    )

    private fun myOverViewItemsWithValidDomesticGreenCard() = MyOverviewItems(
        items = listOf(
            MyOverviewItem.GreenCardItem(
                greenCard = GreenCard(
                    GreenCardEntity(
                        id = 1,
                        walletId = 1,
                        GreenCardType.Domestic
                    ),
                    origins = listOf(
                        originEntity()
                    ),
                    credentialEntities = listOf(
                        credentialEntity()
                    )
                ),
                originStates = listOf(
                    OriginState.Valid(originEntity())
                ),
                credentialState = MyOverviewItem.GreenCardItem.CredentialState.HasCredential(credentialEntity()),
                databaseSyncerResult = DatabaseSyncerResult.Success
            )
        ),
        selectedType = GreenCardType.Domestic
    )

    private fun originEntity(type: OriginType = OriginType.Vaccination) = OriginEntity(
        id = 1,
        greenCardId = 1,
        type = type,
        eventTime = OffsetDateTime.now(eventTimeClock),
        validFrom = OffsetDateTime.now(eventTimeClock),
        expirationTime = OffsetDateTime.now(expireTimeClock),
    )

    private fun credentialEntity() = CredentialEntity(
        id = 1,
        greenCardId = 1,
        data = "".toByteArray(),
        credentialVersion = 2,
        validFrom = OffsetDateTime.now(eventTimeClock),
        expirationTime = OffsetDateTime.now(expireTimeClock),
    )

            private fun triggerNetworkError(viewModel: MyOverviewViewModel, noCredentialsLeft: Boolean = true) =
        triggerSyncerResult(viewModel, DatabaseSyncerResult.Failed.NetworkError(AppErrorResult(Step(1), IllegalStateException()), noCredentialsLeft))

    private fun triggerSyncerResult(
        viewModel: MyOverviewViewModel,
        syncResult: DatabaseSyncerResult
    ) {
        ((viewModel.databaseSyncerResultLiveData) as MutableLiveData).postValue(
            Event(
                syncResult
            )
        )
    }

    private fun launchFragment(selectType: GreenCardType = GreenCardType.Domestic): MyOverviewViewModel {
        val viewModel = object : MyOverviewViewModel() {
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
                factory<CachedAppConfigUseCase> {
                    object : CachedAppConfigUseCase {
                        override fun getCachedAppConfig(): HolderConfig {
                            return HolderConfig.default()
                        }

                        override fun getProviderName(providerIdentifier: String): String {
                            return "GGD"
                        }
                    }
                }
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