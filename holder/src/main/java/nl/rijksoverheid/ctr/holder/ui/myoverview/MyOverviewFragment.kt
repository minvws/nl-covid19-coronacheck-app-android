package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.design.BaseActivity
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewNavigationCardAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewTestResultAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewTestResultExpiredAdapterItem
import nl.rijksoverheid.ctr.introduction.IntroductionViewModel
import nl.rijksoverheid.ctr.shared.ext.executeAfterAllAnimationsAreFinished
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.show
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : BaseFragment(R.layout.fragment_my_overview) {

    private val section = Section()

    private val localTestResultHandler = Handler(Looper.getMainLooper())
    private val localTestResultRunnable = object : Runnable {
        override fun run() {
            localTestResultViewModel.getLocalTestResult()

            // Refresh every 10 seconds
            localTestResultHandler.postDelayed(this, TimeUnit.SECONDS.toMillis(10))
        }
    }

    private val introductionViewModel: IntroductionViewModel by viewModel()
    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!introductionViewModel.introductionFinished()) {
            findNavController().navigate(R.id.action_introduction)
        } else if (requireActivity() is BaseActivity) {
            (requireActivity() as BaseActivity).removeSplashScreen()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)

        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        (binding.recyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        setItems()

        localTestResultViewModel.localTestResultStateLiveData.observe(
            viewLifecycleOwner,
            EventObserver { localTestResultState ->
                when (localTestResultState) {
                    is LocalTestResultState.None -> {
                        // Nothing
                    }
                    is LocalTestResultState.Expired -> {
                        setItems(
                            isExpired = true
                        )
                    }
                    is LocalTestResultState.Valid -> {
                        // Wait until other RecyclerView animations are finished before adding this view
                        // Else it can cause weird glitches
                        binding.recyclerView.executeAfterAllAnimationsAreFinished {
                            setItems(
                                localTestResult = localTestResultState.localTestResult
                            )

                            // Show a SnackBar if this qr is created for the first time
                            if (localTestResultState.firstTimeCreated) {
                                Snackbar.make(
                                    requireView(),
                                    R.string.my_overview_qr_created_snackbar_message,
                                    Snackbar.LENGTH_LONG
                                ).also {
                                    it.setAction(R.string.my_overview_qr_created_snackbar_button) {
                                        BuildConfig.URL_FAQ.launchUrl(requireContext())
                                    }
                                }.show(requireActivity())
                            }
                        }
                    }
                }
            })
    }

    override fun onResume() {
        super.onResume()
        localTestResultHandler.postAtFrontOfQueue(localTestResultRunnable)
    }

    override fun onPause() {
        super.onPause()
        localTestResultHandler.removeCallbacks(localTestResultRunnable)
    }

    private fun setItems(
        isExpired: Boolean = false,
        localTestResult: LocalTestResult? = null,
    ) {
        val items = mutableListOf<BindableItem<*>>()
        items.add(MyOverviewHeaderAdapterItem())
        if (isExpired) {
            items.add(MyOverviewTestResultExpiredAdapterItem(onDismissClick = {
                setItems(
                    isExpired = false
                )
            }))
        }
        localTestResult?.let {
            items.add(
                MyOverviewTestResultAdapterItem(
                    localTestResult = it,
                    onButtonClick = {
                        findNavController().navigate(MyOverviewFragmentDirections.actionQrCode())
                    }
                )
            )
        }
        items.add(MyOverviewNavigationCardAdapterItem(
            title = R.string.my_overview_no_qr_make_appointment_title,
            description = R.string.my_overview_no_qr_make_appointment_description,
            backgroundColor = R.color.light_blue,
            backgroundDrawable = R.drawable.illustration_make_appointment,
            buttonText = R.string.my_overview_no_qr_make_appointment_button,
            onButtonClick = {
                findNavController().navigate(MyOverviewFragmentDirections.actionMakeAppointment())
            }
        ))
        items.add(MyOverviewNavigationCardAdapterItem(
            title = R.string.my_overview_no_qr_make_qr_title,
            description = R.string.my_overview_no_qr_make_qr_description,
            backgroundColor = R.color.green,
            backgroundDrawable = R.drawable.illustration_create_qr,
            buttonText = R.string.my_overview_no_qr_make_qr_button,
            onButtonClick = {
                findNavController().navigate(MyOverviewFragmentDirections.actionChooseProvider())
            }
        ))
        section.update(items)
    }
}
