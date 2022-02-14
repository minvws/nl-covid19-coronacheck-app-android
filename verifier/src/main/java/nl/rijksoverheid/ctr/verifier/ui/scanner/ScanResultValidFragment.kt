package nl.rijksoverheid.ctr.verifier.ui.scanner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.fragment.AutoCloseFragment
import nl.rijksoverheid.ctr.shared.models.VerificationPolicy
import nl.rijksoverheid.ctr.shared.utils.Accessibility
import nl.rijksoverheid.ctr.verifier.BuildConfig
import nl.rijksoverheid.ctr.verifier.R
import nl.rijksoverheid.ctr.verifier.databinding.FragmentScanResultValidBinding
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionState
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionStateUseCase
import nl.rijksoverheid.ctr.verifier.ui.policy.VerificationPolicySelectionUseCase
import nl.rijksoverheid.ctr.verifier.ui.scanner.models.ScanResultValidData
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class ScanResultValidFragment : AutoCloseFragment(0) {

    private var _binding: FragmentScanResultValidBinding? = null
    private val binding get() = _binding!!

    private val verificationPolicySelectionUseCase: VerificationPolicySelectionUseCase by inject()
    private val verificationPolicy: VerificationPolicy by lazy {
        verificationPolicySelectionUseCase.get()
    }

    private val args: ScanResultValidFragmentArgs by navArgs()
    private val verificationPolicySelectionStateUseCase: VerificationPolicySelectionStateUseCase by inject()

    override fun aliveForSeconds(): Long {
        return if (BuildConfig.FLAVOR == "acc") TimeUnit.SECONDS.toMillis(10) else TimeUnit.MILLISECONDS.toMillis(800)
    }

    override fun navigateToCloseAt() {
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
            when (verificationPolicy) {
                is VerificationPolicy.VerificationPolicy3G -> {
                    R.style.AppTheme_Scanner_Valid_3G
                }
                is VerificationPolicy.VerificationPolicy1G -> {
                    R.style.AppTheme_Scanner_Valid_1G
                }
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
                val text = when (verificationPolicySelectionStateUseCase.get()) {
                    is VerificationPolicySelectionState.Policy1G,
                    is VerificationPolicySelectionState.Selection.Policy1G,
                    is VerificationPolicySelectionState.Selection.Policy3G -> getString(
                        R.string.verifier_result_access_title_policy, verificationPolicy.configValue
                    )
                    is VerificationPolicySelectionState.Policy3G,
                    is VerificationPolicySelectionState.Selection.None -> getString(R.string.verifier_result_access_title)
                }
                binding.title.text = text
            }
        }
        Accessibility.announce(requireContext(), binding.title.text.toString())
    }

    override fun onResume() {
        super.onResume()
        args.validData.externalReturnAppData?.let {
            startActivity(it.intent)
            activity?.finishAffinity()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
