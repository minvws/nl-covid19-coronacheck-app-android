package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestTypeBinding
import nl.rijksoverheid.ctr.holder.databinding.IncludeTestCodeTypeBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CommercialTestTypeFragment : Fragment(R.layout.fragment_commercial_test_type) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCommercialTestTypeBinding.bind(view)
        binding.typeCode.bind(R.drawable.ic_test_code, R.string.commercial_test_type_code_title) {
            findNavController().navigate(CommercialTestTypeFragmentDirections.actionCommercialTestCode())
        }
        binding.typeQrCode.bind(
            R.drawable.ic_test_qr_code,
            R.string.commercial_test_type_qr_code_title
        ) {

        }
    }
}

private fun IncludeTestCodeTypeBinding.bind(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    onClick: () -> Unit
) {
    this.icon.setImageResource(icon)
    this.title.setText(title)
    root.setOnClickListener {
        onClick()
    }
}