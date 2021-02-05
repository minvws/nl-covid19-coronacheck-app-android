package nl.rijksoverheid.ctr.holder.myqr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.holder.databinding.FragmentMyQrBinding
import nl.rijksoverheid.ctr.shared.ext.observeResult
import org.koin.android.viewmodel.ext.android.viewModel

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class MyQrFragment : Fragment() {

    private lateinit var binding: FragmentMyQrBinding
    private val myQrViewModel: MyQrViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeResult(myQrViewModel.accessTokenLiveData, {
            // TODO: Implement loading state
        }, { accessToken ->
            myQrViewModel.generateQrCode(
                accessToken,
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
            binding.existingQr.cardQr.setImageBitmap(bitmap)
        }, {
            // TODO: Implement error UI
        })

        binding.noQr.getTestResultsButton.setOnClickListener {
            myQrViewModel.login(requireActivity() as AppCompatActivity)
        }
    }
}
