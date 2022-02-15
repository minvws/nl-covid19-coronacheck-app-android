/*
 *
 *  *  Copyright (c) 2022 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *  *
 *  *   SPDX-License-Identifier: EUPL-1.2
 *  *
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentNewDisclosurePolicyBinding
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

class NewDisclosurePolicyFragment: Fragment(R.layout.fragment_new_disclosure_policy) {

    private val androidUtil: AndroidUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNewDisclosurePolicyBinding.bind(view)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
        }
    }
}