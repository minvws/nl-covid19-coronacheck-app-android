package nl.rijksoverheid.ctr.holder.menu

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import nl.rijksoverheid.ctr.design.fragments.menu.MenuSection
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.usecases.HolderFeatureFlagUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MenuViewModelImplTest : AutoCloseKoinTest() {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val helpMenuDataModel = mockk<HelpMenuDataModel>().apply {
        val context = ApplicationProvider.getApplicationContext<Context>()
        every { get(context) } returns emptyArray()
    }
    private val featureFlagUseCase = mockk<HolderFeatureFlagUseCase>()

    @Test
    fun `on menu click with visitor pass enabled get correct menu items`() {
        every { featureFlagUseCase.getVisitorPassEnabled() } returns true
        val menuViewModel = MenuViewModelImpl(helpMenuDataModel, featureFlagUseCase)

        menuViewModel.click(ApplicationProvider.getApplicationContext())
        val menuSections = menuViewModel.menuSectionLiveData.value!!.peekContent()

        assertEquals(3, menuSections[0].menuItems.size)
        assertEquals(R.drawable.ic_menu_add, menuSections[0].menuItems[0].icon)
        assertEquals(
            R.string.holder_menu_listItem_addVaccinationOrTest_title,
            menuSections[0].menuItems[0].title
        )
        assertTrue(menuSections[0].menuItems[0].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(3, menuSections[0].menuItems.size)
        assertEquals(R.drawable.ic_menu_paper, menuSections[0].menuItems[1].icon)
        assertEquals(R.string.holder_menu_paperproof_title, menuSections[0].menuItems[1].title)
        assertTrue(menuSections[0].menuItems[1].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(R.drawable.ic_menu_briefcase, menuSections[0].menuItems[2].icon)
        assertEquals(R.string.holder_menu_visitorpass, menuSections[0].menuItems[2].title)
        assertTrue(menuSections[0].menuItems[2].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(2, menuSections[1].menuItems.size)
        assertEquals(R.drawable.ic_menu_saved_events, menuSections[1].menuItems[0].icon)
        assertEquals(R.string.holder_menu_storedEvents, menuSections[1].menuItems[0].title)
        assertTrue(menuSections[1].menuItems[0].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(R.drawable.ic_menu_info, menuSections[1].menuItems[1].icon)
        assertEquals(R.string.holder_menu_helpInfo, menuSections[1].menuItems[1].title)
        assertTrue(menuSections[1].menuItems[1].onClick is MenuSection.MenuItem.OnClick.Navigate)
    }

    @Test
    fun `on menu click with visitor pass disabled get correct menu items`() {
        every { featureFlagUseCase.getVisitorPassEnabled() } returns false
        val menuViewModel = MenuViewModelImpl(helpMenuDataModel, featureFlagUseCase)

        menuViewModel.click(ApplicationProvider.getApplicationContext())
        val menuSections = menuViewModel.menuSectionLiveData.value!!.peekContent()

        assertEquals(2, menuSections[0].menuItems.size)
        assertEquals(R.drawable.ic_menu_add, menuSections[0].menuItems[0].icon)
        assertEquals(
            R.string.holder_menu_listItem_addVaccinationOrTest_title,
            menuSections[0].menuItems[0].title
        )
        assertTrue(menuSections[0].menuItems[0].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(R.drawable.ic_menu_paper, menuSections[0].menuItems[1].icon)
        assertEquals(R.string.holder_menu_paperproof_title, menuSections[0].menuItems[1].title)
        assertTrue(menuSections[0].menuItems[1].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(2, menuSections[1].menuItems.size)
        assertEquals(R.drawable.ic_menu_saved_events, menuSections[1].menuItems[0].icon)
        assertEquals(R.string.holder_menu_storedEvents, menuSections[1].menuItems[0].title)
        assertTrue(menuSections[1].menuItems[0].onClick is MenuSection.MenuItem.OnClick.Navigate)
        assertEquals(R.drawable.ic_menu_info, menuSections[1].menuItems[1].icon)
        assertEquals(R.string.holder_menu_helpInfo, menuSections[1].menuItems[1].title)
        assertTrue(menuSections[1].menuItems[1].onClick is MenuSection.MenuItem.OnClick.Navigate)
    }
}
