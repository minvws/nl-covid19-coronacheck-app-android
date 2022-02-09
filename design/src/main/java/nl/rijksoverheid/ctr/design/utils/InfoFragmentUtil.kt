package nl.rijksoverheid.ctr.design.utils

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.design.fragments.info.InfoBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragment
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface InfoFragmentUtil {

    companion object {
        const val EXTRA_INFO_FRAGMENT_DATA = "data"
    }

    fun presentFullScreen(
        currentFragment: Fragment,
        infoFragmentDirections: NavDirections
    )

    fun presentFullScreen(
        currentFragment: Fragment,
        @IdRes infoFragmentNavigationId: Int = R.id.action_info_fragment,
        toolbarTitle: String,
        data: InfoFragmentData)

    fun presentAsBottomSheet(
        fragmentManager: FragmentManager,
        data: InfoFragmentData,
    )
}

class InfoFragmentUtilImpl: InfoFragmentUtil {
    override fun presentFullScreen(
        currentFragment: Fragment,
        infoFragmentDirections: NavDirections
    ) {
        currentFragment.findNavController().navigate(
            infoFragmentDirections
        )
    }

    override fun presentFullScreen(currentFragment: Fragment,
                                   @IdRes infoFragmentNavigationId: Int,
                                   toolbarTitle: String,
                                   data: InfoFragmentData) {
        currentFragment.findNavController().navigate(
            infoFragmentNavigationId,
            bundleOf(
                Pair("toolbarTitle", toolbarTitle),
                Pair(InfoFragmentUtil.EXTRA_INFO_FRAGMENT_DATA, data)
            )
        )
    }

    override fun presentAsBottomSheet(
        fragmentManager: FragmentManager,
        data: InfoFragmentData,
    ) {
        val bottomSheet = InfoBottomSheetDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(InfoFragmentUtil.EXTRA_INFO_FRAGMENT_DATA, data)
            }
        }
        bottomSheet.show(fragmentManager, "bottomSheetTag")
    }
}
