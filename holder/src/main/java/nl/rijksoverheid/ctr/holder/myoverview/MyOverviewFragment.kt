package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.digid.DigiDFragment
import nl.rijksoverheid.ctr.shared.ext.observeResult
import org.koin.android.viewmodel.ext.android.viewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : DigiDFragment() {

    private lateinit var binding: FragmentMyOverviewBinding
    private val qrCodeViewModel: QrCodeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.noQr.createQrCard.createQrCardButton.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionChooseProvider())
        }

        binding.existingQr.createQrCard.createQrCardButton.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionChooseProvider())
        }

        observeResult(qrCodeViewModel.qrCodeLiveData, {
            binding.noQr.root.visibility = View.INVISIBLE
            binding.existingQr.root.visibility = View.VISIBLE
        }, {
            binding.existingQr.qrCardQrImage.setImageBitmap(it)
        }, {
            binding.noQr.root.visibility = View.VISIBLE
            binding.existingQr.root.visibility = View.GONE
            presentError()
        })

        binding.existingQr.qrCardQrImage.doOnPreDraw {
            observeResult(qrCodeViewModel.localTestResultLiveData, {
            }, { localTestResult ->
                if (localTestResult != null) {
                    binding.existingQr.cardFooter.text = getString(
                        R.string.my_overview_existing_qr_date, localTestResult.sampleDate.format(
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        )
                    )

                    lifecycleScope.launchWhenResumed {
                        launch {
                            qrCodeViewModel.generateQrCode(
                                credentials = localTestResult.credentials,
                                qrCodeWidth = binding.existingQr.qrCardQrImage.width,
                                qrCodeHeight = binding.existingQr.qrCardQrImage.height
                            )
                        }
                    }

                }
            }, {
                presentError()
            })
        }

        qrCodeViewModel.getLocalTestResult(OffsetDateTime.now())
    }

}
