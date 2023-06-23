package nl.rijksoverheid.ctr.introduction.onboarding

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.introduction.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentOnboardingBinding
import nl.rijksoverheid.ctr.shared.ext.animationsEnabled
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getNavigationIconView
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private val args: OnboardingFragmentArgs by navArgs()

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentOnboardingBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                args.introductionData.onboardingItems
            )

        if (args.introductionData.onboardingItems.isNotEmpty()) {
            binding.indicators.initIndicator(adapter.itemCount)
            initViewPager(binding, adapter, savedInstanceState?.getInt(indicatorPositionKey))
        }

        setBackPressListener(binding)

        setBindings(binding, adapter)
    }

    private fun setBindings(
        binding: FragmentOnboardingBinding,
        adapter: OnboardingPagerAdapter
    ) {
        binding.toolbar.visibility = View.VISIBLE
        binding.toolbar.setNavigationOnClickListener {
            binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, animationsEnabled())
        }
        binding.logo.isVisible = true
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                navigateSafety(R.id.nav_onboarding,
                    OnboardingFragmentDirections.actionPrivacyPolicy(
                        args.introductionData
                    )
                )
            } else {
                binding.viewPager.setCurrentItem(currentItem + 1, animationsEnabled())
                binding.toolbar.getNavigationIconView()?.setAccessibilityFocus()
            }
        }

        if (Accessibility.touchExploration(context)) {
            binding.toolbar.setAccessibilityFocus()
        }
    }

    private fun setBackPressListener(binding: FragmentOnboardingBinding) {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
            OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentItem = binding.viewPager.currentItem
                if (currentItem == 0) {
                    val canPop = findNavControllerSafety()?.popBackStack() ?: false
                    if (!canPop) {
                        requireActivity().finish()
                    }
                } else {
                    binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, animationsEnabled())
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
        binding: FragmentOnboardingBinding,
        adapter: OnboardingPagerAdapter,
        startingItem: Int? = null
    ) {
        binding.viewPager.offscreenPageLimit = args.introductionData.onboardingItems.size
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("StringFormatInvalid")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.toolbar.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                binding.logo.isFocusable = (position == 0)
                binding.logo.importantForAccessibility = if (position == 0) {
                    View.IMPORTANT_FOR_ACCESSIBILITY_YES
                } else {
                    View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
                binding.indicators.updateSelected(position)

                // Apply bottom elevation if the view inside the viewpager is scrollable
                val scrollView =
                    childFragmentManager.fragments[position]?.view?.findViewById<ScrollView>(R.id.scroll)
                if (scrollView?.canScrollVertically(1) == true) {
                    binding.bottom.cardElevation =
                        resources.getDimensionPixelSize(R.dimen.scroll_view_button_elevation)
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
