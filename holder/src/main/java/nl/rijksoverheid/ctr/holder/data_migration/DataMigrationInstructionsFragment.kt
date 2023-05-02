/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import nl.rijksoverheid.ctr.design.fragments.ErrorResultFragment
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.introduction.databinding.FragmentOnboardingBinding
import nl.rijksoverheid.ctr.introduction.onboarding.OnboardingPagerAdapter
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.ErrorResultFragmentData

class DataMigrationInstructionsFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val args: DataMigrationInstructionsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.FLAVOR.lowercase().contains("prod")) {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        increaseBrightness()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        _binding = FragmentOnboardingBinding.bind(view)

        val adapter =
            OnboardingPagerAdapter(
                childFragmentManager,
                lifecycle,
                args.instructionItems.toList()
            )

        binding.indicators.initIndicator(adapter.itemCount)
        initViewPager(adapter, savedInstanceState?.getInt(indicatorPositionKey))

        setBackPressListener()
        setBindings(adapter)
    }

    private fun setBindings(
        adapter: OnboardingPagerAdapter
    ) {
        binding.button.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem == adapter.itemCount - 1) {
                if (args.destination is DataMigrationOnboardingItem.ScanQrCode) {
                    openScanner()
                } else {
                    navigateSafety(args.destination.navigationActionId)
                }
            } else {
                binding.viewPager.currentItem = currentItem + 1
            }
        }
    }

    private fun openScanner() {
        try {
            val cameraManager =
                requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (cameraManager.cameraIdList.isNotEmpty()) {
                navigateSafety(DataMigrationInstructionsFragmentDirections.actionDataMigrationScanQr())
            } else {
                showNoCameraError()
            }
        } catch (exception: CameraAccessException) {
            showNoCameraError()
        }
    }

    private fun showNoCameraError() {
        navigateSafety(
            R.id.action_error_result,
            ErrorResultFragment.getBundle(
                ErrorResultFragmentData(
                    title = getString(R.string.add_paper_proof_no_camera_error_header),
                    description = getString(R.string.add_paper_proof_no_camera_error_description),
                    buttonTitle = getString(R.string.back_to_overview),
                    buttonAction = ErrorResultFragmentData.ButtonAction.Destination(R.id.action_my_overview)
                )
            )
        )
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
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            @SuppressLint("StringFormatInvalid")
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.button.text = if (position == 2) {
                    getString(R.string.holder_startMigration_onboarding_doneButton)
                } else {
                    getString(R.string.holder_startMigration_onboarding_nextButton)
                }

                binding.indicators.updateSelected(position)

                binding.indicators.contentDescription = getString(
                    nl.rijksoverheid.ctr.introduction.R.string.onboarding_page_indicator_label,
                    (position + 1).toString(),
                    adapter.itemCount.toString()
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

                if (args.instructionItems[position].clazz == DataMigrationShowQrCodeFragment::class.java) {
                    increaseBrightness()
                } else {
                    resetBrightness()
                }
            }
        })
        startingItem?.let { binding.viewPager.currentItem = it }
    }

    fun increaseBrightness() {
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        requireActivity().window.attributes = params
    }

    private fun resetBrightness() {
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        requireActivity().window.attributes = params
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        resetBrightness()

        requireActivity().requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    companion object {
        private const val indicatorPositionKey = "indicator_position_key"
    }
}
