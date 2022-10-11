package nl.rijksoverheid.ctr.design.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.fragments.info.InfoBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil.Companion.EXTRA_INFO_FRAGMENT_DATA
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil.Companion.HIDE_ICON
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil.Companion.TOOLBAR_TITLE

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface InfoFragmentUtil {

    companion object {
        const val TOOLBAR_TITLE = "toolbarTitle"
        const val EXTRA_INFO_FRAGMENT_DATA = "data"
        const val HIDE_ICON = "hideIcon"
    }

    fun presentFullScreen(
        currentFragment: Fragment,
        infoFragmentDirections: NavDirections
    )

    fun presentFullScreen(
        currentFragment: Fragment,
        @IdRes infoFragmentNavigationId: Int = R.id.action_info_fragment,
        toolbarTitle: String,
        data: InfoFragmentData,
        hideNavigationIcon: Boolean = false
    )

    fun presentAsBottomSheet(
        fragmentManager: FragmentManager,
        data: InfoFragmentData
    )
}

class InfoFragmentUtilImpl : InfoFragmentUtil {
    override fun presentFullScreen(
        currentFragment: Fragment,
        infoFragmentDirections: NavDirections
    ) {
        currentFragment.findNavController().navigate(
            infoFragmentDirections
        )
    }

    override fun presentFullScreen(
        currentFragment: Fragment,
        @IdRes infoFragmentNavigationId: Int,
        toolbarTitle: String,
        data: InfoFragmentData,
        hideNavigationIcon: Boolean
    ) {
        currentFragment.findNavController().navigate(
            infoFragmentNavigationId,
            bundleOf(
                TOOLBAR_TITLE to toolbarTitle,
                EXTRA_INFO_FRAGMENT_DATA to data,
                HIDE_ICON to hideNavigationIcon
            )
        )
    }

    override fun presentAsBottomSheet(
        fragmentManager: FragmentManager,
        data: InfoFragmentData
    ) {
        val bottomSheet = InfoBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_INFO_FRAGMENT_DATA, data)
            }
        }
        bottomSheet.show(fragmentManager, "bottomSheetTag")
    }
}
