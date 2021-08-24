/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.verifier.ui.instructions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.introduction.IntroductionData
import nl.rijksoverheid.ctr.introduction.ui.onboarding.OnboardingPagerAdapter
import nl.rijksoverheid.ctr.introduction.ui.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.VerifierMainFragment
import nl.rijksoverheid.ctr.verifier.databinding.FragmentInstructionsBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class InstructionsFragment : Fragment(R.layout.fragment_instructions) {

    private val scannerUtil: ScannerUtil by inject()

    private var _binding: FragmentInstructionsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentInstructionsBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                getExplanationData().onboardingItems
            )

        if (getExplanationData().onboardingItems.isNotEmpty()) {
            binding.indicators.initIndicator(adapter.itemCount)
            initViewPager(binding, adapter, savedInstanceState?.getInt(indicatorPositionKey))
        }

        setBackPressListener(binding)

        setBindings(binding, adapter)
    }

    private fun setBindings(
        binding: FragmentInstructionsBinding,
        adapter: OnboardingPagerAdapter
    ) {
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                scannerUtil.launchScanner(requireActivity())
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        // Nullable so tests don't trip over parentFragment
        (parentFragment?.parentFragment as VerifierMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.scan_instructions_toolbar)
                    setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.action_skip_instructions -> {
                                scannerUtil.launchScanner(requireActivity())
                            }
                        }
                        true
                    }
                }
            }
        }
    }

    private fun setBackPressListener(binding: FragmentInstructionsBinding) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (currentItem == 0) {
                    val canPop = findNavController().popBackStack()
                    if (!canPop) {
                        requireActivity().finish()
                    }
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
        binding: FragmentInstructionsBinding,
        adapter: OnboardingPagerAdapter,
        startingItem: Int? = null,
    ) {
        binding.viewPager.offscreenPageLimit = getExplanationData().onboardingItems.size
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

                if (position == adapter.itemCount - 1) {
                    binding.button.text = getString(R.string.scan_qr_button)
                } else {
                    binding.button.text = getString(R.string.onboarding_next)
                }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        if (parentFragment?.parentFragment is VerifierMainFragment) {
            (parentFragment?.parentFragment as VerifierMainFragment).let {
                it.getToolbar()
                it.getToolbar().menu.clear()
                // Reset menu item listener to default
                it.resetMenuItemListener()
            }
        }
    }

    companion object {
        private const val indicatorPositionKey = "indicator_position_key"
    }

    private fun getExplanationData(): IntroductionData {
        return IntroductionData(
            onboardingItems = listOf(
                OnboardingItem(
                    R.drawable.illustration_scaninstructions_1,
                    R.string.scan_instructions_1_title,
                    R.string.scan_instructions_1_description,
                    position = 1
                ),
                OnboardingItem(
                    R.drawable.illustration_scaninstructions_2,
                    R.string.scan_instructions_2_title,
                    R.string.scan_instructions_2_description,
                    position = 2
                ),
                OnboardingItem(
                    R.drawable.illustration_scaninstructions_3,
                    R.string.scan_instructions_3_title,
                    R.string.scan_instructions_3_description,
                    position = 3
                ),
                OnboardingItem(
                    R.drawable.illustration_scaninstructions_4,
                    R.string.scan_instructions_4_title,
                    R.string.scan_instructions_4_description,
                    position = 4
                ),
            )
        )
    }
}
