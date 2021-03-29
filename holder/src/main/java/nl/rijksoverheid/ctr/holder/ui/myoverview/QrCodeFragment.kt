package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.FullScreenDialogFragment
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogQrCodeBinding
import nl.rijksoverheid.ctr.shared.QrCodeConstants
import nl.rijksoverheid.ctr.shared.ext.setAccessibilityFocus
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeFragment : FullScreenDialogFragment(R.layout.dialog_qr_code) {

    private var _binding: DialogQrCodeBinding? = null
    private val binding: DialogQrCodeBinding by lazy { _binding!! }
    private val localTestResultViewModel: LocalTestResultViewModel by sharedViewModel()
    private val qrCodeHandler = Handler(Looper.getMainLooper())
    private val qrCodeRunnable = object : Runnable {
        override fun run() {
            val canGenerateQrCode = localTestResultViewModel.generateQrCode(
                size = resources.displayMetrics.widthPixels
            )
            if (canGenerateQrCode) {
                val refreshMillis =
                    if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else (QrCodeConstants.VALID_FOR_SECONDS / 2) * 1000
                qrCodeHandler.postDelayed(this, refreshMillis)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = DialogQrCodeBinding.bind(view)

        val params = dialog?.window?.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        dialog?.window?.attributes = params

        localTestResultViewModel.qrCodeLiveData.observe(viewLifecycleOwner) {
            binding.image.setImageBitmap(it.qrCode)
            presentQrLoading(false)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun presentQrLoading(loading: Boolean) {
        binding.loading.visibility = if (loading) View.VISIBLE else View.GONE
        binding.content.visibility = if (loading) View.GONE else View.VISIBLE
        // Move focus to loading indicator or QR depending on state
        if (loading) {
           binding.loading.setAccessibilityFocus()
        } else {
            binding.image.setAccessibilityFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        presentQrLoading(true)
        qrCodeHandler.post(qrCodeRunnable)

        // If the qr code has expired close this screen
        val localTestResult = localTestResultViewModel.retrievedLocalTestResult
        if (localTestResult == null) {
            // No credentials in cache, go back to overview
            findNavController().popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        qrCodeHandler.removeCallbacks(qrCodeRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
