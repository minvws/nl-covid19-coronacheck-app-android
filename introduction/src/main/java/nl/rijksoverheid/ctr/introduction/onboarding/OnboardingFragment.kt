package nl.rijksoverheid.ctr.introduction.onboarding

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.introduction.CoronaCheckApp
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentOnboardingBinding

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val onboardingData by lazy { (requireActivity().application as CoronaCheckApp).getOnboardingData() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnboardingActivityBackground()

        val binding = FragmentOnboardingBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                onboardingData.onboardingItems
            )
        binding.viewPager.offscreenPageLimit = onboardingData.onboardingItems.size
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.toolbar.visibility = if (position == 0) View.GONE else View.VISIBLE
                updateCurrentIndicator(binding, position)

                // Apply bottom elevation if the view inside the viewpager is scrollable
                val scrollView =
                    childFragmentManager.fragments[position]?.view?.findViewById<ScrollView>(R.id.scroll)
                if (scrollView?.canScrollVertically(1) == true) {
                    binding.bottom.cardElevation =
                        resources.getDimensionPixelSize(R.dimen.onboarding_bottom_scroll_elevation)
                            .toFloat()
                } else {
                    binding.bottom.cardElevation = 0f
                }

            }
        })

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

        binding.toolbar.setNavigationOnClickListener {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }

        binding.button.text = getString(onboardingData.onboardingNextButtonStringResource)
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                findNavController().navigate(OnboardingFragmentDirections.actionPrivacyPolicy())
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        initIndicators(binding, adapter)
        binding.viewPager.setCurrentItem(0, false) // Triggers onPageSelected
    }

    private fun initIndicators(
        binding: FragmentOnboardingBinding,
        adapter: OnboardingPagerAdapter
    ) {
        val padding = resources.getDimensionPixelSize(R.dimen.onboarding_item_indicator_spacing)
        repeat(adapter.itemCount) {
            val indicator = AppCompatImageView(requireContext())
            indicator.setPadding(padding, padding, padding, padding)
            indicator.setImageResource(R.drawable.shape_onboarding_item_indicator)
            binding.indicators.addView(indicator)
        }
    }

    /**
     * Remove splash screen and set
     */
    private fun setOnboardingActivityBackground() {
        val theme = ContextThemeWrapper(
            requireActivity(),
            nl.rijksoverheid.ctr.design.R.style.AppTheme
        ).theme
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        requireActivity().window.setBackgroundDrawableResource(R.drawable.introduction_launch_screen)
    }

    private fun updateCurrentIndicator(binding: FragmentOnboardingBinding, position: Int) {
        binding.indicators.forEachIndexed { index, view ->
            val color = if (index == position) {
                ContextCompat.getColor(requireContext(), R.color.onboarding_indicator_selected)
            } else {
                ContextCompat.getColor(requireContext(), R.color.onboarding_indicator)
            }
            (view as ImageView).setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
    }
}
