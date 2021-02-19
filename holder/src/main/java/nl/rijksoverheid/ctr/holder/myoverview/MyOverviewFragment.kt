package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.holder.BaseFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.models.LocalTestResult
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : BaseFragment(R.layout.fragment_my_overview) {

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

        binding.createQrCard.createQrCardButton.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionTestAppointmentInfo())
        }

        binding.qrCard.root.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionQrCode())
        }

        localTestResultViewModel.localTestResultLiveData.observe(viewLifecycleOwner, EventObserver {
            presentLocalTestResult(binding, it)
        })

        qrCodeViewModel.qrCodeLiveData.observe(viewLifecycleOwner, EventObserver {
            binding.qrCard.qrCardQrImage.setImageBitmap(it)
        })

        localTestResultViewModel.getLocalTestResult(OffsetDateTime.now())
    }

    private fun presentLocalTestResult(
        binding: FragmentMyOverviewBinding,
        localTestResult: LocalTestResult
    ) {
        binding.qrCard.cardFooter.text = getString(
            R.string.my_overview_existing_qr_date, localTestResult.expireDate.format(
                DateTimeFormatter.ofPattern("dd MMMM HH:mm", Locale.getDefault())
            )
        )

        binding.qrCard.root.visibility = View.VISIBLE

        binding.qrCard.qrCardQrImage.doOnPreDraw {
            lifecycleScope.launchWhenResumed {
                localTestResultViewModel.retrievedLocalTestResult?.credentials?.let { credentials ->
                    qrCodeViewModel.generateQrCode(
                        credentials = credentials,
                        qrCodeSize = binding.qrCard.qrCardQrImage.width,
                    )
                }
            }
        }
    }
}
