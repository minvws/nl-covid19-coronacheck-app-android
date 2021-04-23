package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.models.LocalTestResultState
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewNavigationCardAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewTestResultAdapterItem
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.MyOverviewTestResultExpiredAdapterItem
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import nl.rijksoverheid.ctr.shared.ext.show
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : Fragment(R.layout.fragment_my_overview) {

    companion object {
        const val REQUEST_KEY = "REQUEST_KEY"
        const val EXTRA_BACK_FROM_QR = "EXTRA_BACK_FROM_QR"
    }

    private val section = Section()

    private val localTestResultHandler = Handler(Looper.getMainLooper())
    private val localTestResultRunnable = Runnable { getLocalTestResult() }

    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)

        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
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
                                    getString(R.string.url_faq).launchUrl(requireContext())
                                }
                            }.show(requireActivity())
                        }
                    }
                }
            })

        setFragmentResultListener(
            REQUEST_KEY
        ) { requestKey, bundle ->
            if (requestKey == REQUEST_KEY && bundle.getBoolean(
                    EXTRA_BACK_FROM_QR
                )
            ) {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun getLocalTestResult() {
        localTestResultViewModel.getLocalTestResult()
        localTestResultHandler.postDelayed(localTestResultRunnable, TimeUnit.SECONDS.toMillis(10))
    }

    override fun onResume() {
        super.onResume()
        getLocalTestResult()
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
                        findNavControllerSafety(R.id.nav_my_overview)?.navigate(
                            MyOverviewFragmentDirections.actionQrCode()
                        )
                    }
                )
            )
        }
        items.add(MyOverviewNavigationCardAdapterItem(
            title = R.string.my_overview_no_qr_make_appointment_title,
            description = R.string.my_overview_no_qr_make_appointment_description,
            backgroundColor = R.color.secondary_blue,
            backgroundDrawable = R.drawable.illustration_make_appointment,
            buttonText = R.string.my_overview_no_qr_make_appointment_button,
            onButtonClick = {
                findNavControllerSafety(R.id.nav_my_overview)?.navigate(
                    MyOverviewFragmentDirections.actionMakeAppointment()
                )
            }
        ))
        items.add(MyOverviewNavigationCardAdapterItem(
            title = if (localTestResult == null) R.string.my_overview_no_qr_make_qr_title else R.string.my_overview_no_qr_replace_qr_title,
            description = R.string.my_overview_no_qr_make_qr_description,
            backgroundColor = R.color.secondary_green,
            backgroundDrawable = R.drawable.illustration_create_qr,
            buttonText = if (localTestResult == null) R.string.my_overview_no_qr_make_qr_button else R.string.my_overview_no_qr_replace_qr_button,
            onButtonClick = {
                findNavControllerSafety(R.id.nav_my_overview)?.navigate(
                    MyOverviewFragmentDirections.actionChooseProvider()
                )
            }
        ))
        section.update(items)
    }
}
