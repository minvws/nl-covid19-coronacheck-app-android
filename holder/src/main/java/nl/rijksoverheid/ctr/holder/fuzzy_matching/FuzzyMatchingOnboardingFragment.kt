package nl.rijksoverheid.ctr.holder.fuzzy_matching

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentFuzzyMatchingOnboardingBinding
import nl.rijksoverheid.ctr.introduction.onboarding.OnboardingPagerAdapter
import nl.rijksoverheid.ctr.introduction.onboarding.models.OnboardingItem
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class FuzzyMatchingOnboardingFragment : Fragment(R.layout.fragment_fuzzy_matching_onboarding) {
    private var _binding: FragmentFuzzyMatchingOnboardingBinding? = null
    private val binding get() = _binding!!

    private val onboardingItems by lazy {
        listOf(
            OnboardingItem(
                imageResource = R.drawable.ic_holder_fuzzymatching_onboarding_firstpage,
                titleResource = R.string.holder_fuzzyMatching_onboarding_firstPage_title,
                description = R.string.holder_fuzzyMatching_onboarding_firstPage_body
            ),
            OnboardingItem(
                imageResource = R.drawable.ic_holder_fuzzymatching_onboarding_secondpage,
                titleResource = R.string.holder_fuzzyMatching_onboarding_secondPage_title,
                description = R.string.holder_fuzzyMatching_onboarding_secondPage_body
            ),
            OnboardingItem(
                imageResource = R.drawable.ic_holder_fuzzymatching_onboarding_thirdpage,
                titleResource = R.string.holder_fuzzyMatching_onboarding_thirdPage_title,
                description = R.string.holder_fuzzyMatching_onboarding_thirdPage_body
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentFuzzyMatchingOnboardingBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                onboardingItems
            )

        if (onboardingItems.isNotEmpty()) {
            binding.indicators.initIndicator(adapter.itemCount)
            initViewPager(adapter, savedInstanceState?.getInt(indicatorPositionKey))
        }

        setBackPressListener()
        setBindings(adapter)
    }

    private fun setBindings(
        adapter: OnboardingPagerAdapter
    ) {
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                navigateSafety(FuzzyMatchingOnboardingFragmentDirections.actionHolderNameSelection())
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }
    }

    private fun setBackPressListener() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (currentItem == 0) {
                    findNavControllerSafety()?.popBackStack()
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
        adapter: OnboardingPagerAdapter,
        startingItem: Int? = null
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
                binding.button.text = getString(
                    if (isLastItem) {
                        R.string.holder_fuzzyMatching_onboarding_thirdPage_action
                    } else {
                        R.string.onboarding_next
                    }
                )

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

    companion object {
        private const val indicatorPositionKey = "indicator_position_key"
    }
}
