package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidBinding
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultValidData
import java.util.concurrent.TimeUnit

class ScanResultValidFragment : Fragment(R.layout.fragment_scan_result_valid) {

    private var _binding: FragmentScanResultValidBinding? = null
    private val binding get() = _binding!!

    private val args: ScanResultValidFragmentArgs by navArgs()

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable {
        navigateSafety(
            R.id.nav_scan_result_valid,
            ScanResultValidFragmentDirections.actionNavQrScanner()
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentScanResultValidBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener {
            navigateSafety(ScanResultValidFragmentDirections.actionNavQrScanner())
        }

        when (args.validData) {
            is ScanResultValidData.Demo -> {
                binding.title.text = getString(R.string.scan_result_demo_title)
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_2
                    )
                )
            }
            is ScanResultValidData.Valid -> {
                binding.title.text = getString(R.string.scan_result_valid_title)
                binding.root.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.secondary_green
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val autoCloseDurationMilli =
            if (BuildConfig.FLAVOR == "tst") TimeUnit.SECONDS.toMillis(10) else 800
        args.validData.externalReturnAppData?.let {
            try {
                startActivity(it.intent)
                activity?.finishAffinity()
            } catch (exception: ActivityNotFoundException) {
                autoCloseHandler.postDelayed(autoCloseRunnable, autoCloseDurationMilli)
            }
        } ?: autoCloseHandler.postDelayed(autoCloseRunnable, autoCloseDurationMilli)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        autoCloseHandler.removeCallbacks(autoCloseRunnable)
    }
}
