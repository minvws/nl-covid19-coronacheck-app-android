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
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
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
    private val qrCodeViewModel: QrCodeViewModel by sharedViewModel(
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_home),
                this
            )
        }
    )

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
        binding.createQrCard.createQrCardButton.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionChooseProvider())
        }

        binding.createQrCard.createQrCardButton.setOnClickListener {
            findNavController().navigate(MyOverviewFragmentDirections.actionChooseProvider())
        }

        observeResult(qrCodeViewModel.qrCodeLiveData, {
            binding.qrCard.qrCardLoading
        }, {
            binding.qrCard.qrCardQrImage.setImageBitmap(it)
        }, {
            presentError()
        })

        binding.qrCard.qrCardQrImage.doOnPreDraw {
            observeResult(qrCodeViewModel.localTestResultLiveData, {
            }, { localTestResult ->
                if (localTestResult != null) {
                    binding.qrCard.cardFooter.text = getString(
                        R.string.my_overview_existing_qr_date, localTestResult.expireDate.format(
                            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                        )
                    )

                    binding.qrCard.root.visibility = View.VISIBLE

                    binding.qrCard.qrCardQrImage.doOnPreDraw {
                        lifecycleScope.launchWhenResumed {
                            launch {
                                qrCodeViewModel.generateQrCode(
                                    credentials = localTestResult.credentials,
                                    qrCodeWidth = binding.qrCard.qrCardQrImage.width,
                                    qrCodeHeight = binding.qrCard.qrCardQrImage.height
                                )
                            }
                        }
                    }
                }
            }, {
                presentError()
            })

            qrCodeViewModel.getLocalTestResult(OffsetDateTime.now())
        }
    }
}
