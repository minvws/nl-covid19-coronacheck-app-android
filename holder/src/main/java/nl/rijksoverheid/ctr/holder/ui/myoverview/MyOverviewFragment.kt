package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.RemoteEvents
import nl.rijksoverheid.ctr.holder.ui.myoverview.items.*
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResult
import nl.rijksoverheid.ctr.holder.ui.myoverview.models.LocalTestResultState
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.scope.emptyState
import timber.log.Timber
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
    private val localTestResultRunnable = Runnable { getLocalTestResult(); sync() }

    // New viewmodel that supports database backed events
    private val myOverviewViewModel: MyOverviewViewModel by sharedViewModelWithOwner(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    // Old viewmodel that works via shared pref stored single test result
    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModelWithOwner(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_graph_overview),
                this
            )
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMyOverviewBinding.bind(view)
        val adapter = GroupAdapter<GroupieViewHolder>().also {
            it.add(section)
        }
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = null
        setItems()

        binding.include.button.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionShowTravelMode())
        }

        // Nullable so tests don't trip over parentFragment
        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.overview_toolbar)
                }
            }
        }

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
                        (parentFragment?.parentFragment as HolderMainFragment?)?.changeMenuItem(
                            menuItemId = R.id.nav_create_qr,
                            text = R.string.create_qr_explanation_menu_title_alternative
                        )
                        setItems(
                            localTestResult = localTestResultState.localTestResult
                        )
                    }
                }
            })

        myOverviewViewModel.walletLiveData.observe(viewLifecycleOwner, {
            Timber.v("Wallet: $it")
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

    private fun sync() {
        myOverviewViewModel.sync()
    }

    private fun getLocalTestResult() {
        localTestResultViewModel.getLocalTestResult()
        sync()
        localTestResultHandler.postDelayed(localTestResultRunnable, TimeUnit.SECONDS.toMillis(10))
    }

    override fun onResume() {
        super.onResume()
        getLocalTestResult()
    }

    override fun onPause() {
        super.onPause()
        localTestResultHandler.removeCallbacks(localTestResultRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).getToolbar().menu.clear()
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
                    MyOverviewFragmentDirections.actionCreateQr()
                )
            }
        ))
        section.update(items)
    }
}
