package nl.rijksoverheid.ctr.holder.introduction.onboarding

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.holder.HideToolbar
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentOnboardingBinding
import nl.rijksoverheid.ctr.holder.introduction.IntroductionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class OnboardingFragment : Fragment(), HideToolbar {

    private val introductionViewModel: IntroductionViewModel by viewModel()
    private lateinit var binding: FragmentOnboardingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            OnboardingPagerAdapter(
                this
            )
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.toolbar.visibility = if (position == 0) View.GONE else View.VISIBLE
                updateCurrentIndicator(position)
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

        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                introductionViewModel.setOnboardingFinished()
                findNavController().navigate(OnboardingFragmentDirections.actionPrivacyPolicy())
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }

        initIndicators(adapter)
        binding.viewPager.setCurrentItem(0, false) // Triggers onPageSelected
    }

    private fun initIndicators(adapter: OnboardingPagerAdapter) {
        val padding = resources.getDimensionPixelSize(R.dimen.onboarding_item_indicator_spacing)
        repeat(adapter.itemCount) {
            val indicator = AppCompatImageView(requireContext())
            indicator.setPadding(padding, padding, padding, padding)
            indicator.setImageResource(R.drawable.shape_onboarding_item_indicator)
            binding.indicators.addView(indicator)
        }
    }

    private fun updateCurrentIndicator(position: Int) {
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
