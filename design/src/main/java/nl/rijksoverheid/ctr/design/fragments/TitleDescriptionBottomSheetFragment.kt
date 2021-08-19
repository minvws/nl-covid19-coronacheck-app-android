package nl.rijksoverheid.ctr.design.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.design.databinding.FragmentTitleDescriptionBottomSheetBinding

class TitleDescriptionBottomSheetFragment: ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return FragmentTitleDescriptionBottomSheetBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTitleDescriptionBottomSheetBinding.bind(view)

        arguments?.getString("title")?.let {
            binding.title.text = it
        }

        arguments?.getString("description")?.let {
            binding.description.setHtmlText(it, htmlLinksEnabled = true)
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