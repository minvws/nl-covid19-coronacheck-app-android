package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.databinding.FragmentCommercialTestCodeBinding
import nl.rijksoverheid.ctr.holder.ext.hideKeyboard
import nl.rijksoverheid.ctr.holder.ext.showKeyboard
import nl.rijksoverheid.ctr.shared.ext.observeResult
import org.koin.android.viewmodel.ext.android.sharedViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class CommercialTestCodeFragment : BaseFragment() {

    private lateinit var binding: FragmentCommercialTestCodeBinding
    private val testResultViewModel: TestResultsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommercialTestCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboard(binding.uniqueCodeText)

        binding.uniqueCodeText.addTextChangedListener {
            checkEnableButton()
        }

        binding.verificationCodeText.addTextChangedListener {
            checkEnableButton()
        }

        binding.button.setOnClickListener {
            hideKeyboard()

            testResultViewModel.getTestResult(
                uniqueCode = binding.uniqueCodeText.text.toString(),
                verificationCode = binding.verificationCodeText.text.toString()
            )

            observeResult(testResultViewModel.testResultLiveData, {
                presentLoading(true)
            }, {
                findNavController().navigate(CommercialTestCodeFragmentDirections.actionYourNegativeResult())
                presentLoading(false)
            }, {
                presentError()
                presentLoading(false)
            })
        }
    }

    private fun checkEnableButton() {
        binding.button.isEnabled =
            binding.uniqueCodeText.text?.isNotEmpty() ?: false && binding.verificationCodeText.text?.isNotEmpty() ?: false
    }
}
