package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentQrCreatedBinding
import nl.rijksoverheid.ctr.shared.ext.formatDateDayMonth
import nl.rijksoverheid.ctr.shared.ext.formatHourMinutes
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.scope.emptyState

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCreatedFragment : Fragment() {

    private lateinit var binding: FragmentQrCreatedBinding
    private val viewModel: TestResultsViewModel by sharedViewModel(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_commercial_test),
                this
            )
        })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQrCreatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val result = viewModel.retrievedResult?.remoteTestResult?.result
        if (result == null) {
            // restored from state, no result anymore
            findNavController().navigate(QrCreatedFragmentDirections.actionMyOverview())
        } else {
            val dayMonths = result.sampleDate.formatDateDayMonth()
            val hourMinutes = result.sampleDate.formatHourMinutes(requireContext())
            binding.description.text = getString(
                R.string.create_qr_code_description, dayMonths, hourMinutes
            ).fromHtml()
        }

        binding.button.setOnClickListener {
            viewModel.saveTestResult()
            findNavController().navigate(QrCreatedFragmentDirections.actionMyOverview())
        }
    }
}
