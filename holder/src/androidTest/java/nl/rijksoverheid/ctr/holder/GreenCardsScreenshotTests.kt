package nl.rijksoverheid.ctr.holder

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
import nl.rijksoverheid.ctr.appconfig.models.AppStatus
import nl.rijksoverheid.ctr.holder.dashboard.DashboardFragment
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardItem
import nl.rijksoverheid.ctr.holder.dashboard.models.DashboardTabItem
import nl.rijksoverheid.ctr.holder.dashboard.models.GreenCardEnabledState
import nl.rijksoverheid.ctr.holder.dashboard.util.OriginState
import nl.rijksoverheid.ctr.persistence.database.DatabaseSyncerResult
import nl.rijksoverheid.ctr.persistence.database.entities.*
import nl.rijksoverheid.ctr.persistence.database.models.GreenCard
import nl.rijksoverheid.ctr.shared.models.GreenCardDisclosurePolicy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import java.time.OffsetDateTime

@RunWith(AndroidJUnit4::class)
class GreenCardsScreenshotTests: ScreenshotTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun DomesticGreenCardWithZeroGPolicy_Screenshot() {
        val originEntity = OriginEntity(
            type = OriginType.Test,
            greenCardId = 1,
            eventTime = OffsetDateTime.now(),
            expirationTime = OffsetDateTime.now().plusDays(500),
            validFrom = OffsetDateTime.now().minusDays(500),
            doseNumber = 0
        )
        val credentialEntity = CredentialEntity(
            greenCardId = 1,
            data = "".toByteArray(),
            credentialVersion = 1,
            validFrom = OffsetDateTime.now().minusDays(100),
            expirationTime = OffsetDateTime.now().plusDays(100)
        )

        val fragmentScenario = startFragment(
            listOf(DashboardTabItem(
                title = R.string.travel_button_domestic,
                greenCardType = GreenCardType.Domestic,
                items = listOf(
                    DashboardItem.CardsItem(
                        cards = listOf(
                            DashboardItem.CardsItem.CardItem(
                                greenCard = GreenCard(
                                    greenCardEntity = GreenCardEntity(
                                        type = GreenCardType.Domestic,
                                        walletId = 1
                                    ),
                                    origins = listOf(
                                        originEntity
                                    ),
                                    credentialEntities = listOf(credentialEntity)
                                ),
                                originStates = listOf(
                                    OriginState.Valid(originEntity)
                                ),
                                credentialState = DashboardItem.CardsItem.CredentialState.HasCredential(credentialEntity),
                                databaseSyncerResult = DatabaseSyncerResult.Success(),
                                disclosurePolicy = GreenCardDisclosurePolicy.ThreeG,
                                greenCardEnabledState = GreenCardEnabledState.Enabled
                            )
                        )
                    )
                )
            ))
        )
        compareScreenshot(fragmentScenario.waitForFragment())
    }

    fun startActivity(args: Bundle = Bundle()): HolderMainActivity {
        val intent = Intent(ApplicationProvider.getApplicationContext(), HolderMainActivity::class.java)
        intent.putExtras(args)
        val scenario = ActivityScenario.launch<HolderMainActivity>(intent)
        return scenario!!.waitForActivity()
    }

    private fun startFragment(tabItems: List<DashboardTabItem>): FragmentScenario<DashboardFragment> {
        loadKoinModules(
            module(override = true) {
                viewModel { fakeAppConfigViewModel(appStatus = AppStatus.NoActionRequired) }
                viewModel { fakeDashboardViewModel(tabItems) }
            }
        )
        val fragmentArgs = bundleOf(
            "returnUri" to "test",
        )
        return launchFragmentInContainer(
            fragmentArgs = fragmentArgs,
            themeResId = R.style.TestAppTheme
        )
    }
}