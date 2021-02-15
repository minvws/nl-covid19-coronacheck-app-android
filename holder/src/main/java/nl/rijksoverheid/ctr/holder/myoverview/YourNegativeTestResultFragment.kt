package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentYourNegativeTestResultsBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class YourNegativeTestResultFragment : Fragment(R.layout.fragment_your_negative_test_results) {

    private val testResultViewModel: TestResultsViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentYourNegativeTestResultsBinding.bind(view)
        // observeResult(testResultViewModel.testResultLiveData, {
        //
        // }, {
        //     // val result = (it as? TestResult.Success)?.remoteTestResult ?: error("Expected a test result")
        //     // binding.rowSubtitle.text =
        //     //     result.result.sampleDate.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM))
        // }, {
        //
        // })

        binding.button.setOnClickListener {
            findNavController().navigate(YourNegativeTestResultFragmentDirections.actionMyOverview())
        }
    }

}
