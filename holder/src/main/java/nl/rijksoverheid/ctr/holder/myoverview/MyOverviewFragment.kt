package nl.rijksoverheid.ctr.holder.myoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyOverviewBinding
import nl.rijksoverheid.ctr.holder.digid.DigiDFragment
import nl.rijksoverheid.ctr.shared.ext.observeResult
import org.koin.android.viewmodel.ext.android.viewModel
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyOverviewFragment : DigiDFragment() {

    private lateinit var binding: FragmentMyOverviewBinding
    private val myQrViewModel: MyOverviewViewModel by viewModel()

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

        observeResult(digidViewModel.accessTokenLiveData, {
            // TODO: Implement loading state
        }, {
            myQrViewModel.generateQrCode(
                it,
                binding.existingQr.cardQr.width,
                binding.existingQr.cardQr.height
            )
        }, {
            // TODO: Implement error UI
        })

        observeResult(myQrViewModel.qrCodeLiveData, {
            binding.noQr.root.visibility = View.GONE
            binding.existingQr.root.visibility = View.VISIBLE
        }, { bitmap ->
            binding.existingQr.cardLoading.visibility = View.GONE
            binding.existingQr.cardQrImage.setImageBitmap(bitmap)
            binding.existingQr.cardFooter.text = getString(
                R.string.my_overview_existing_qr_date,
                OffsetDateTime.now().format(DateTimeFormatter.ISO_ORDINAL_DATE)
            )
        }, {
            Timber.v("TEST")
            // TODO: Implement error UI
        })

        binding.noQr.card2Button.setOnClickListener {
            login()
        }
    }
}
