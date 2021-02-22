package nl.rijksoverheid.ctr.holder.myoverview

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.SimpleItemAnimator
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewHeaderAdapterItem
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewNavigationCardAdapterItem
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewTestResultAdapterItem
import nl.rijksoverheid.ctr.holder.myoverview.items.MyOverviewTestResultExpiredAdapterItem
import nl.rijksoverheid.ctr.holder.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : BaseFragment(R.layout.fragment_my_overview) {

    private val section = Section()


    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_home),
                this
            )
        }
    )
    private val qrCodeViewModel: QrCodeViewModel by viewModel()

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
                        setItems(
                            localTestResult = localTestResultState.localTestResult
                        )

                        lifecycleScope.launchWhenResumed {
                            qrCodeViewModel.generateQrCode(
                                localTestResult = localTestResultState.localTestResult,
                                qrCodeSize = resources.displayMetrics.widthPixels
                            )
                        }
                    }
                }
            })

        qrCodeViewModel.qrCodeLiveData.observe(viewLifecycleOwner, EventObserver {
            setItems(
                localTestResult = it.localTestResult,
                qrCode = it.qrCode
            )
        })

        localTestResultViewModel.getLocalTestResult()
    }

    private fun setItems(
        isExpired: Boolean = false,
        localTestResult: LocalTestResult? = null,
        qrCode: Bitmap? = null,
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
                    qrCode = qrCode,
                    onQrCodeClick = {
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
