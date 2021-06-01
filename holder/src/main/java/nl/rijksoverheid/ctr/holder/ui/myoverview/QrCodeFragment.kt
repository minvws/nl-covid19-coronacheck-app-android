package nl.rijksoverheid.ctr.holder.ui.myoverview

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.parcelize.Parcelize
import nl.rijksoverheid.ctr.holder.BuildConfig
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentQrCodeBinding
import nl.rijksoverheid.ctr.shared.QrCodeConstants
import nl.rijksoverheid.ctr.shared.ext.sharedViewModelWithOwner
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAccessibilityFocus
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.scope.emptyState
import timber.log.Timber
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit


/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class QrCodeFragment : Fragment(R.layout.fragment_qr_code) {

    private var _binding: FragmentQrCodeBinding? = null
    private val binding get() = _binding!!
    private val args: QrCodeFragmentArgs by navArgs()

    private val qrCodeHandler = Handler(Looper.getMainLooper())
    private val qrCodeRunnable = Runnable {
        generateQrCode()
        checkIfCredentialExpired()
    }

    private val qrCodeViewModel: QrCodeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.FLAVOR == "prod") {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        requireActivity().window.attributes = params
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        _binding = FragmentQrCodeBinding.bind(view)

        qrCodeViewModel.qrCodeLiveData.observe(viewLifecycleOwner) {
            binding.image.setImageBitmap(it)
            presentQrLoading(false)
        }

        // Nullable so tests don't trip over parentFragment
        (parentFragment?.parentFragment as HolderMainFragment?)?.getToolbar().let { toolbar ->
            if (toolbar?.menu?.size() == 0) {
                toolbar.apply {
                    inflateMenu(R.menu.my_qr_toolbar)

                    setOnMenuItemClickListener {
                        if (it.itemId == R.id.action_show_qr_explanation) {
                            findNavController().navigate(QrCodeFragmentDirections.actionShowQrExplanation())
                        }
                        true
                    }
                }
            }
        }
    }

    private fun presentQrLoading(loading: Boolean) {
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(loading)
        binding.content.visibility = if (loading) View.GONE else View.VISIBLE
        // Move focus to loading indicator or QR depending on state
        if (!loading) {
            binding.image.setAccessibilityFocus()
        }
    }

    private fun generateQrCode() {
        qrCodeViewModel.generateQrCode(
            size = resources.displayMetrics.widthPixels,
            credential = args.data.credential,
            shouldDisclose = args.data.shouldDisclose
        )
        val refreshMillis =
            if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else (QrCodeConstants.VALID_FOR_SECONDS / 2) * 1000
        qrCodeHandler.postDelayed(qrCodeRunnable, refreshMillis)
    }

    /**
     * If the QR is expired we close this fragment
     * The [MyOverviewFragment] should correctly handle new or expired credentials
     */
    private fun checkIfCredentialExpired() {
        val expirationTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(args.data.credentialExpirationTimeSeconds), ZoneOffset.UTC)
        if (OffsetDateTime.now(ZoneOffset.UTC).isAfter(expirationTime)) {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        presentQrLoading(true)
        generateQrCode()
    }

    override fun onPause() {
        super.onPause()
        qrCodeHandler.removeCallbacks(qrCodeRunnable)
        (parentFragment?.parentFragment as HolderMainFragment).let{
            it.getToolbar().menu.clear()
            // Reset menu item listener to default
            it.resetMenuItemListener()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Set brightness back to previous
        val params = requireActivity().window.attributes
        params?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        requireActivity().window.attributes = params

        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)

        setFragmentResult(
            MyOverviewFragment.REQUEST_KEY,
            bundleOf(MyOverviewFragment.EXTRA_BACK_FROM_QR to true)
        )
    }
}