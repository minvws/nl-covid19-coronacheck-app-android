package nl.rijksoverheid.ctr.appconfig.app_update

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.appconfig.AppConfigViewModel
import nl.rijksoverheid.ctr.appconfig.R
import nl.rijksoverheid.ctr.appconfig.databinding.FragmentNewFeaturesBinding
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.getNavigationIconView
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewFeaturesFragment : Fragment(R.layout.fragment_new_features) {

    private val args: NewFeaturesFragmentArgs by navArgs()
    private val appConfigViewModel: AppConfigViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNewFeaturesBinding.bind(view)

        val adapter = NewFeaturesPagerAdapter(
            childFragmentManager,
            lifecycle,
            args.appUpdateData.newFeatures
        )

        if (args.appUpdateData.newFeatures.isNotEmpty()) {
            binding.indicators.initIndicator(adapter.itemCount)
            initViewPager(binding, adapter)
        }

        bindViews(binding, adapter)
    }

    private fun bindViews(
        binding: FragmentNewFeaturesBinding,
        adapter: NewFeaturesPagerAdapter
    ) {
        binding.run {
            toolbar.setNavigationOnClickListener {
                viewPager.currentItem = viewPager.currentItem - 1
            }
            button.setOnClickListener {
                val currentItem = viewPager.currentItem
                if (currentItem == adapter.itemCount - 1) finishFlow() else showNextPage(currentItem, binding)
            }
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object :
                OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val currentItem = viewPager.currentItem
                    if (currentItem == 0) {
                        val canPop = findNavControllerSafety()?.popBackStack() ?: false
                        if (!canPop) {
                            requireActivity().finish()
                        }
                    } else {
                        viewPager.currentItem = viewPager.currentItem - 1
                    }
                }
            })
        }
    }

    private fun FragmentNewFeaturesBinding.showNextPage(
        currentItem: Int,
        binding: FragmentNewFeaturesBinding
    ) {
        val nextIndex = currentItem + 1
        viewPager.currentItem = nextIndex
        setButtonText(binding, nextIndex)
        toolbar.getNavigationIconView()?.setAccessibilityFocus()
    }

    private fun finishFlow() {
        args.appUpdateData.savePolicyChange()
        appConfigViewModel.saveNewFeaturesFinished()

        navigateToMain()
    }

    private fun navigateToTerms() {
        navigateSafety(
            R.id.nav_new_features,
            NewFeaturesFragmentDirections.actionNewTerms(args.appUpdateData)
        )
    }

    private fun navigateToMain() {
        requireActivity().findNavController(R.id.main_nav_host_fragment)
            .navigate(R.id.action_main)
    }

    private fun initViewPager(
        binding: FragmentNewFeaturesBinding,
        adapter: NewFeaturesPagerAdapter
    ) {
        binding.viewPager.offscreenPageLimit = args.appUpdateData.newFeatures.size
        binding.viewPager.adapter = adapter
        val hideToolbar = adapter.itemCount == 1
        if (hideToolbar) {
            binding.toolbar.visibility = View.INVISIBLE
        }
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("StringFormatInvalid")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (!hideToolbar) {
                    binding.toolbar.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                }
                binding.indicators.updateSelected(position)

                binding.indicators.contentDescription = getString(
                    R.string.onboarding_page_indicator_label,
                    (position + 1).toString(),
                    adapter.itemCount.toString()
                )

                setButtonText(binding, position)

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
    }

    private fun setButtonText(binding: FragmentNewFeaturesBinding, position: Int) {
        binding.button.text = getString(args.appUpdateData.newFeatures[position].buttonResource)
    }
}