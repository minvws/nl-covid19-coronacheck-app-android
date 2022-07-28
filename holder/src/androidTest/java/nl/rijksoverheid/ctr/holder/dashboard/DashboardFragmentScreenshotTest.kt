/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.os.bundleOf
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.karumi.shot.ActivityScenarioUtils.waitForActivity
import com.karumi.shot.FragmentScenarioUtils.waitForFragment
import com.karumi.shot.ScreenshotTest
import nl.rijksoverheid.ctr.fakeDashboardViewModel
import nl.rijksoverheid.ctr.holder.fakeOriginEntity
import nl.rijksoverheid.ctr.holder.AndroidTestUtils
import nl.rijksoverheid.ctr.holder.HolderMainActivity
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.holder.fakeCredentialEntity
import nl.rijksoverheid.ctr.holder.fakeMobileCoreWrapper
import nl.rijksoverheid.ctr.holder.models.HolderStep
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardEntity
import nl.rijksoverheid.ctr.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.models.AppErrorResult
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class DashboardFragmentScreenshotTest : ScreenshotTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun Loading_DomesticGreenCardWithOneGPolicy_Screenshot() {
        val greenCardEntity = GreenCardEntity(
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOriginEntity = fakeOriginEntity(
            type = OriginType.Vaccination,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val recoveryOriginEntity = fakeOriginEntity(
            type = OriginType.Recovery,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val testOriginEntity = fakeOriginEntity(
            type = OriginType.Test,
            validFrom = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val credentialEntity = fakeCredentialEntity()

        val cardItem = DashboardItem.CardsItem(
            cards = listOf(
                DashboardItem.CardsItem.CardItem(
                    greenCard = GreenCard(
                        greenCardEntity = greenCardEntity,
                        origins = listOf(
                            vaccinationOriginEntity,
                            recoveryOriginEntity,
                            testOriginEntity
                        ),
                        credentialEntities = listOf(credentialEntity)
                    ),
                    originStates = listOf(
                        OriginState.Valid(vaccinationOriginEntity),
                        OriginState.Valid(recoveryOriginEntity),
                        OriginState.Valid(testOriginEntity)
                    ),
                    credentialState = DashboardItem.CardsItem.CredentialState.LoadingCredential,
                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                    disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                    greenCardEnabledState = GreenCardEnabledState.Enabled
                )
            )
        )

        val fragmentScenario = startFragment(
            items = listOf(cardItem)
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun Invalid_ServerError_FirstTime_DomesticGreenCardWithOneGPolicy_Screenshot() {
        val greenCardEntity = GreenCardEntity(
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOriginEntity = fakeOriginEntity(
            type = OriginType.Vaccination,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val recoveryOriginEntity = fakeOriginEntity(
            type = OriginType.Recovery,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val testOriginEntity = fakeOriginEntity(
            type = OriginType.Test,
            validFrom = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val credentialEntity = fakeCredentialEntity()

        val cardItem = DashboardItem.CardsItem(
            cards = listOf(
                DashboardItem.CardsItem.CardItem(
                    greenCard = GreenCard(
                        greenCardEntity = greenCardEntity,
                        origins = listOf(
                            vaccinationOriginEntity,
                            recoveryOriginEntity,
                            testOriginEntity
                        ),
                        credentialEntities = listOf(credentialEntity)
                    ),
                    originStates = listOf(
                        OriginState.Valid(vaccinationOriginEntity),
                        OriginState.Valid(recoveryOriginEntity),
                        OriginState.Valid(testOriginEntity)
                    ),
                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                    databaseSyncerResult = DatabaseSyncerResult.Failed.ServerError.FirstTime(
                        AppErrorResult(
                            HolderStep.GetCredentialsNetworkRequest,
                            IllegalStateException("")
                        )
                    ),
                    disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                    greenCardEnabledState = GreenCardEnabledState.Enabled
                )
            )
        )

        val fragmentScenario = startFragment(
            items = listOf(cardItem)
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun Invalid_ServerError_MultipleTimes_DomesticGreenCardWithOneGPolicy_Screenshot() {
        val greenCardEntity = GreenCardEntity(
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOriginEntity = fakeOriginEntity(
            type = OriginType.Vaccination,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val recoveryOriginEntity = fakeOriginEntity(
            type = OriginType.Recovery,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val testOriginEntity = fakeOriginEntity(
            type = OriginType.Test,
            validFrom = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val credentialEntity = fakeCredentialEntity()

        val cardItem = DashboardItem.CardsItem(
            cards = listOf(
                DashboardItem.CardsItem.CardItem(
                    greenCard = GreenCard(
                        greenCardEntity = greenCardEntity,
                        origins = listOf(
                            vaccinationOriginEntity,
                            recoveryOriginEntity,
                            testOriginEntity
                        ),
                        credentialEntities = listOf(credentialEntity)
                    ),
                    originStates = listOf(
                        OriginState.Valid(vaccinationOriginEntity),
                        OriginState.Valid(recoveryOriginEntity),
                        OriginState.Valid(testOriginEntity)
                    ),
                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                    databaseSyncerResult = DatabaseSyncerResult.Failed.ServerError.MultipleTimes(
                        AppErrorResult(
                            HolderStep.GetCredentialsNetworkRequest,
                            IllegalStateException("")
                        )
                    ),
                    disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                    greenCardEnabledState = GreenCardEnabledState.Enabled
                )
            )
        )

        val fragmentScenario = startFragment(
            items = listOf(cardItem)
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun Invalid_ServerError_MultipleTimes_TwoEuGreenCardsWithOneGPolicy_Screenshot() {
        val greenCardEntity = GreenCardEntity(
            walletId = 1,
            type = GreenCardType.Eu
        )

        val vaccinationOriginEntity = fakeOriginEntity(
            type = OriginType.Vaccination,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val recoveryOriginEntity = fakeOriginEntity(
            type = OriginType.Recovery,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val credentialEntity = fakeCredentialEntity()

        val vaccinationCardItem = DashboardItem.CardsItem(
            cards = listOf(
                DashboardItem.CardsItem.CardItem(
                    greenCard = GreenCard(
                        greenCardEntity = greenCardEntity,
                        origins = listOf(vaccinationOriginEntity),
                        credentialEntities = listOf(credentialEntity)
                    ),
                    originStates = listOf(
                        OriginState.Valid(vaccinationOriginEntity)
                    ),
                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                    databaseSyncerResult = DatabaseSyncerResult.Failed.ServerError.MultipleTimes(
                        AppErrorResult(
                            HolderStep.GetCredentialsNetworkRequest,
                            IllegalStateException("")
                        )
                    ),
                    disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                    greenCardEnabledState = GreenCardEnabledState.Enabled
                )
            )
        )

        val recoveryCardItem =
            DashboardItem.CardsItem(
                cards = listOf(
                    DashboardItem.CardsItem.CardItem(
                        greenCard = GreenCard(
                            greenCardEntity = greenCardEntity,
                            origins = listOf(recoveryOriginEntity),
                            credentialEntities = listOf(credentialEntity)
                        ),
                        originStates = listOf(
                            OriginState.Valid(recoveryOriginEntity)
                        ),
                        credentialState = DashboardItem.CardsItem.CredentialState.HasCredential(
                            credentialEntity
                        ),
                        databaseSyncerResult = DatabaseSyncerResult.Failed.ServerError.MultipleTimes(
                            AppErrorResult(
                                HolderStep.GetCredentialsNetworkRequest,
                                IllegalStateException("")
                            )
                        ),
                        disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                        greenCardEnabledState = GreenCardEnabledState.Enabled
                    )
                )
            )

        val fragmentScenario = startFragment(
            items = listOf(vaccinationCardItem, recoveryCardItem)
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    @Test
    fun Future_DomesticGreenCardWithOneGPolicy_Screenshot() {
        val greenCardEntity = GreenCardEntity(
            walletId = 1,
            type = GreenCardType.Domestic
        )

        val vaccinationOriginEntity = fakeOriginEntity(
            type = OriginType.Vaccination,
            validFrom = AndroidTestUtils.getOffsetDateTime("2020-01-01T00:00:00.00Z"),
            expirationTime = AndroidTestUtils.getOffsetDateTime("2030-01-01T00:00:00.00Z")
        )

        val cardItem = DashboardItem.CardsItem(
            cards = listOf(
                DashboardItem.CardsItem.CardItem(
                    greenCard = GreenCard(
                        greenCardEntity = greenCardEntity,
                        origins = listOf(vaccinationOriginEntity),
                        credentialEntities = listOf()
                    ),
                    originStates = listOf(
                        OriginState.Future(vaccinationOriginEntity)
                    ),
                    credentialState = DashboardItem.CardsItem.CredentialState.NoCredential,
                    databaseSyncerResult = DatabaseSyncerResult.Success(),
                    disclosurePolicy = GreenCardDisclosurePolicy.OneG,
                    greenCardEnabledState = GreenCardEnabledState.Enabled
                )
            )
        )

        val fragmentScenario = startFragment(
            items = listOf(cardItem)
        )

        compareScreenshot(fragmentScenario.waitForFragment())
    }

    fun startActivity(args: Bundle = Bundle()): HolderMainActivity {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), HolderMainActivity::class.java)
        intent.putExtras(args)
        val scenario = ActivityScenario.launch<HolderMainActivity>(intent)
        return scenario!!.waitForActivity()
    }

    private fun startFragment(
        items: List<DashboardItem.CardsItem>
    ): FragmentScenario<DashboardFragment> {
        val tabItem = DashboardTabItem(
            title = R.string.app_name,
            greenCardType = items.first().cards.first().greenCard.greenCardEntity.type,
            items = items
        )

        loadKoinModules(
            module(override = true) {
                viewModel { fakeDashboardViewModel(listOf(tabItem)) }
                factory { fakeMobileCoreWrapper }
            }
        )

        return launchFragmentInContainer(
            fragmentArgs = bundleOf("returnUri" to "test"),
            themeResId = R.style.TestAppTheme
        )
    }
}
