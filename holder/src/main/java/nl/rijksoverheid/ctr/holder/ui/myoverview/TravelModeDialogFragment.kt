/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentTravelModeBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.GreenCardType
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import org.koin.androidx.viewmodel.ViewModelOwner

class TravelModeDialogFragment : ExpandedBottomSheetDialogFragment() {

    private val myOverviewViewModel: MyOverviewViewModel by sharedViewModelWithOwner(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return FragmentTravelModeBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTravelModeBinding.bind(view)

        binding.close.setOnClickListener {
            dismiss()
        }

        binding.buttonDomestic.setOnClickListener {
            myOverviewViewModel.refreshOverviewItems(GreenCardType.Domestic)
            binding.buttonDomestic.setToggled(true)
            binding.buttonForeign.setToggled(false)
            setFooter(binding, GreenCardType.Domestic)
        }

        binding.buttonForeign.setOnClickListener {
            myOverviewViewModel.refreshOverviewItems(GreenCardType.Eu)
            binding.buttonDomestic.setToggled(false)
            binding.buttonForeign.setToggled(true)
            setFooter(binding, GreenCardType.Eu)
        }

        ViewCompat.setAccessibilityDelegate(binding.close, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                info?.setTraversalBefore(binding.description)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })

        setFooter(binding, myOverviewViewModel.getSelectedType())
        when (myOverviewViewModel.getSelectedType()) {
            is GreenCardType.Domestic -> {
                binding.buttonDomestic.setToggled(true)
            }
            is GreenCardType.Eu -> {
                binding.buttonForeign.setToggled(true)
            }
        }
    }

    private fun setFooter(binding: FragmentTravelModeBinding, greenCardType: GreenCardType) {
        when (greenCardType) {
            is GreenCardType.Domestic -> {
                binding.footer.setText(R.string.travel_fragment_footer)
            }
            is GreenCardType.Eu -> {
                binding.footer.setText(R.string.travel_fragment_footer_eu)
            }
        }
    }

}
