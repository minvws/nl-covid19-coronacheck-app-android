package nl.rijksoverheid.ctr.verifier.ui.instructions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.introduction.ui.onboarding.OnboardingPagerAdapter
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainFragment
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanInstructionsBinding
import nl.rijksoverheid.ctr.verifier.models.ScannerState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionType
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionStateUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScanQrViewModel
import nl.rijksoverheid.ctr.verifier.ui.scanqr.ScannerNavigationState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanInstructionsFragment : Fragment(R.layout.fragment_scan_instructions) {

    private val scannerUtil: ScannerUtil by inject()
    private val scanQrViewModel: ScanQrViewModel by viewModel()
    private val scanInstructionsButtonUtil: ScanInstructionsButtonUtil by inject()
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase by inject()
    private var _binding: FragmentScanInstructionsBinding? = null
    private val binding get() = _binding!!

    private val onboardingItems by lazy { onboardingItemList(verificationPolicySelectionStateUseCase.get()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentScanInstructionsBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                onboardingItems
            )

        if (onboardingItems.isNotEmpty()) {
            binding.indicators.initIndicator(adapter.itemCount)
            initViewPager(binding, adapter, savedInstanceState?.getInt(indicatorPositionKey))
        }

        setBackPressListener(binding)
        setBindings(binding, adapter)

        scanQrViewModel.scannerNavigationStateEvent.observe(viewLifecycleOwner, EventObserver{
            closeInstructionsAndOpenNextScreen(it)
        })
    }

    private fun setBindings(
        binding: FragmentScanInstructionsBinding,
        adapter: OnboardingPagerAdapter
    ) {
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                clearToolbar()
                // Instructions have been opened, set as seen
                scanQrViewModel.setScanInstructionsSeen()
                scanQrViewModel.nextScreen()
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        setupToolbarMenu()
    }

    private fun closeInstructionsAndOpenNextScreen(state: ScannerNavigationState) {
        when (state) {
            is ScannerNavigationState.Scanner -> {
                if (!state.isLocked) {
                    findNavControllerSafety()?.popBackStack(R.id.nav_scan_qr, false)
                    scannerUtil.launchScanner(requireActivity(), arguments?.getString("returnUri"))
                } else {
                    navigateSafety(R.id.nav_scan_qr, bundleOf("returnUri" to arguments?.getString("returnUri")))
                }
            }
            is ScannerNavigationState.NewPolicyRules -> {
                navigateSafety(
                    ScanInstructionsFragmentDirections.actionNewPolicyRules(
                        returnUri = arguments?.getString("returnUri")
                    )
                )
            }
            else -> {
                navigateSafety(
                    ScanInstructionsFragmentDirections.actionPolicySelection(
                        selectionType = VerificationPolicySelectionType.FirstTimeUse(ScannerState.Unlocked(VerificationPolicySelectionState.Selection.None)),
                        toolbarTitle = getString(R.string.verifier_menu_risksetting),
                        returnUri = arguments?.getString("returnUri"),
                    )
                )
            }
        }
    }

    private fun setBackPressListener(binding: FragmentScanInstructionsBinding) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (currentItem == 0) {
                    findNavControllerSafety()?.popBackStack()
                    clearToolbar()
                } else {
                    binding.viewPager.currentItem = binding.viewPager.currentItem - 1
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            outState.putInt(indicatorPositionKey, it.viewPager.currentItem)
        }
    }

    private fun initViewPager(
        binding: FragmentScanInstructionsBinding,
        adapter: OnboardingPagerAdapter,
        startingItem: Int? = null,
    ) {
        binding.viewPager.offscreenPageLimit = onboardingItems.size
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("StringFormatInvalid")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.indicators.updateSelected(position)

                binding.indicators.contentDescription = getString(
                    nl.rijksoverheid.ctr.introduction.R.string.onboarding_page_indicator_label,
                    (position + 1).toString(),
                    adapter.itemCount.toString()
                )
                val isLastItem = position == adapter.itemCount - 1
                if (isLastItem) {
                    clearToolbar()
                } else {
                    setupToolbarMenu()
                }
                binding.button.text = getString(scanInstructionsButtonUtil.getButtonText(isLastItem))

                // Apply bottom elevation if the view inside the viewpager is scrollable
                val scrollView =
                    childFragmentManager.fragments[position]?.view?.findViewById<ScrollView>(nl.rijksoverheid.ctr.introduction.R.id.scroll)
                if (scrollView?.canScrollVertically(1) == true) {
                    binding.bottom.cardElevation =
                        resources.getDimensionPixelSize(nl.rijksoverheid.ctr.introduction.R.dimen.scroll_view_button_elevation)
                            .toFloat()
                } else {
                    binding.bottom.cardElevation = 0f
                }
            }
        })
        startingItem?.let { binding.viewPager.currentItem = it }
    }

    private fun setupToolbarMenu() {
        // Check if parent is VerifierMainFragment so we can reuse the toolbar
        (parentFragment?.parentFragment as? VerifierMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    // only show Skip option if user hasn't seen the instructions before
                    if (!scanQrViewModel.hasSeenScanInstructions()) {
                        inflateMenu(R.menu.scan_instructions_toolbar)
                        setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.action_skip_instructions -> {
                                    clearToolbar()
                                    // Instructions have been opened, set as seen
                                    scanQrViewModel.setScanInstructionsSeen()
                                    scanQrViewModel.nextScreen()
                                }
                            }
                            true
                        }
                    }
                }
            }
        }
    }

    private fun clearToolbar() {
        // Remove added toolbar item(s) so they don't show up in other screens
        (parentFragment?.parentFragment as? VerifierMainFragment).let {
            it?.getToolbar()?.menu?.clear()
        }
    }

    override fun onPause() {
        super.onPause()
        clearToolbar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val indicatorPositionKey = "indicator_position_key"
    }
}
