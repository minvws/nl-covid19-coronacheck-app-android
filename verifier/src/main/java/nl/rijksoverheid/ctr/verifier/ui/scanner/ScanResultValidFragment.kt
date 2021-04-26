package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.findNavControllerSafety
import nl.rijksoverheid.ctr.shared.util.PersonalDetailsUtil
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultValidData
import nl.rijksoverheid.ctr.verifier.ui.scanner.util.ScannerUtil
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class ScanResultValidFragment : Fragment(R.layout.fragment_scan_result_valid) {

    private var _binding: FragmentScanResultValidBinding? = null
    private val binding get() = _binding!!

    private val args: ScanResultValidFragmentArgs by navArgs()
    private val scannerUtil: ScannerUtil by inject()
    private val personalDetailsUtil: PersonalDetailsUtil by inject()

    private val transitionPersonalDetailsHandler = Handler(Looper.getMainLooper())
    private val transitionPersonalDetailsRunnable = Runnable {
        presentPersonalDetails()
    }

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable {
        findNavControllerSafety(R.id.nav_scan_result_valid)?.navigate(ScanResultValidFragmentDirections.actionNavMain())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentScanResultValidBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(ScanResultValidFragmentDirections.actionNavMain())
        }

        when (args.validData) {
            is ScanResultValidData.Demo -> {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_2
                    )
                )
            }
            is ScanResultValidData.Valid -> {
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary_green
                    )
                )
            }
        }

        binding.personalDetails.scroll.doOnLayout {
            if (binding.personalDetails.scroll.canScrollVertically(1)) {
                binding.personalDetails.bottom.cardElevation =
                    resources.getDimensionPixelSize(nl.rijksoverheid.ctr.introduction.R.dimen.onboarding_bottom_scroll_elevation)
                        .toFloat()
            } else {
                binding.personalDetails.bottom.cardElevation = 0f
            }
        }

        binding.personalDetails.icon.setOnClickListener {
            findNavController().navigate(ScanResultValidFragmentDirections.actionShowValidExplanation())
        }

        binding.personalDetails.button.setOnClickListener {
            scannerUtil.launchScanner(requireActivity())
        }

        if (binding.personalDetails.root.visibility == View.GONE) {
            binding.root.setOnClickListener {
                presentPersonalDetails()
            }
        }

        transitionPersonalDetailsHandler.postDelayed(
            transitionPersonalDetailsRunnable,
            TimeUnit.MILLISECONDS.toMillis(800)
        )
    }

    private fun presentPersonalDetails() {
        binding.personalDetails.root.visibility = View.VISIBLE
        binding.toolbar.setTitle(R.string.scan_result_valid_title)
        val testResultAttributes = args.validData.verifiedQr.testResultAttributes
        val personalDetails = personalDetailsUtil.getPersonalDetails(
            testResultAttributes.firstNameInitial,
            testResultAttributes.lastNameInitial,
            testResultAttributes.birthDay,
            testResultAttributes.birthMonth,
            includeBirthMonthNumber = true
        )
        binding.personalDetails.lastNameInitial.text = personalDetails.lastNameInitial
        binding.personalDetails.firstNameInitial.text = personalDetails.firstNameInitial
        binding.personalDetails.birthMonth.text = personalDetails.birthMonth
        binding.personalDetails.birthDay.text = personalDetails.birthDay
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
        _binding = null
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
        transitionPersonalDetailsHandler.removeCallbacks(autoCloseRunnable)
    }
}
