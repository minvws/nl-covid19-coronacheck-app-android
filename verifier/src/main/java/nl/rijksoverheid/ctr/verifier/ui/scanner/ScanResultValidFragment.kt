package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.appconfig.usecases.FeatureFlagUseCase
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidBinding
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicyUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultValidData
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit


class ScanResultValidFragment : Fragment() {

    private var _binding: FragmentScanResultValidBinding? = null
    private val binding get() = _binding!!

    private val verificationPolicyUseCase: VerificationPolicyUseCase by inject()
    private val verificationPolicy: VerificationPolicy by lazy {
        verificationPolicyUseCase.get()
    }

    private val args: ScanResultValidFragmentArgs by navArgs()
    private val featureFlagUseCase: FeatureFlagUseCase by inject()

    private val autoCloseHandler = Handler(Looper.getMainLooper())
    private val autoCloseRunnable = Runnable {
        navigateSafety(
            R.id.nav_scan_result_valid,
            ScanResultValidFragmentDirections.actionNavQrScanner()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val theme = if (args.validData is ScanResultValidData.Demo) {
            R.style.AppTheme_Scanner_Valid_Demo
        } else {
            if (featureFlagUseCase.isVerificationPolicyEnabled()) {
                when (verificationPolicy) {
                    is VerificationPolicy.VerificationPolicy3G -> {
                        R.style.AppTheme_Scanner_Valid_3G
                    }
                    is VerificationPolicy.VerificationPolicy2G -> {
                        R.style.AppTheme_Scanner_Valid_2G
                    }
                    is VerificationPolicy.VerificationPolicy2GPlus -> {
                        R.style.AppTheme_Scanner_Valid_2GPlus
                    }
                }
            } else {
                R.style.AppTheme_Scanner_Valid_3G
            }
        }

        val context = ContextThemeWrapper(requireContext(), theme)
        val layoutInflater = inflater.cloneInContext(context)
        _binding = FragmentScanResultValidBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            navigateSafety(ScanResultValidFragmentDirections.actionNavQrScanner())
        }

        when (args.validData) {
            is ScanResultValidData.Demo -> {
                binding.title.text = getString(R.string.scan_result_demo_title)
            }
            is ScanResultValidData.Valid -> {
                val text = if (featureFlagUseCase.isVerificationPolicyEnabled()) {
                    when (verificationPolicy) {
                        is VerificationPolicy.VerificationPolicy2G -> {
                            getString(R.string.verifier_result_access_title_highrisk)
                        }
                        is VerificationPolicy.VerificationPolicy3G -> {
                            getString(R.string.verifier_result_access_title_lowrisk)
                        }
                        is VerificationPolicy.VerificationPolicy2GPlus -> {
                            getString(R.string.verifier_result_access_title_2g_plus)
                        }
                    }
                } else {
                    getString(R.string.verifier_result_access_title)
                }
                binding.title.text = text
            }
        }
        Accessibility.announce(requireContext(), binding.title.text.toString())
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
