package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.utils.BottomSheetData
import nl.rijksoverheid.ctr.design.utils.BottomSheetDialogUtil
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultInvalidBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultInvalidData
import nl.rijksoverheid.ctr.verifier.ui.scanner.utils.ScannerUtil
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ScanResultInvalidFragment : Fragment(R.layout.fragment_scan_result_invalid) {

    private val scannerUtil: ScannerUtil by inject()
    private val bottomSheetDialogUtil: BottomSheetDialogUtil by inject()

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable { navigateToScanner() }

    private val args: ScanResultInvalidFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentScanResultInvalidBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener { navigateToScanner() }

        when (args.invalidData) {
            is ScanResultInvalidData.Invalid -> {
                binding.title.text = getString(R.string.scan_result_european_nl_invalid_title)
                binding.buttonExplanation.visibility = View.INVISIBLE
            }
            is ScanResultInvalidData.Error -> {
                binding.buttonExplanation.setOnClickListener {
                    bottomSheetDialogUtil.present(childFragmentManager,
                        BottomSheetData.TitleDescription(
                            title = getString(R.string.scan_result_invalid_reason_title),
                            applyOnDescription = {
                                it.setHtmlText(R.string.scan_result_invalid_reason_description)
                            }
                        ))
                }
            }
        }

        binding.buttonNext.setOnClickListener { navigateToScanner() }
    }

    private fun navigateToScanner() {
        navigateSafety(
            R.id.nav_scan_result_invalid,
            ScanResultInvalidFragmentDirections.actionNavQrScanner(
                returnUri = (args.invalidData as? ScanResultInvalidData.Error)?.returnUri
            )
        )
    }

    override fun onResume() {
        super.onResume()
        val autoCloseDuration =
            if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else TimeUnit.MINUTES.toMillis(
                3
            )
        autoCloseHandler.postDelayed(autoCloseRunnable, autoCloseDuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
    }
}
