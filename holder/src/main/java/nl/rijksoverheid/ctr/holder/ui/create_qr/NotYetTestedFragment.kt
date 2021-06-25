package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import nl.rijksoverheid.ctr.appconfig.usecases.CachedAppConfigUseCase
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogNotYetTestedBinding
import nl.rijksoverheid.ctr.shared.ext.launchUrl
import org.koin.android.ext.android.inject

class NotYetTestedFragment : ExpandedBottomSheetDialogFragment() {

    private val cachedAppConfigUseCase: CachedAppConfigUseCase by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return DialogNotYetTestedBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogNotYetTestedBinding.bind(view)
        binding.description.setHtmlText(getString(
            R.string.not_yet_tested_description,
            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toString(),
            cachedAppConfigUseCase.getCachedAppConfigMaxValidityHours().toString()))

        binding.button.setOnClickListener {
            getString(R.string.url_make_appointment).launchUrl(requireContext())
        }

        binding.close.setOnClickListener {
            dismiss()
        }

        ViewCompat.setAccessibilityDelegate(binding.close, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                info?.setTraversalBefore(binding.title)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })
    }
}