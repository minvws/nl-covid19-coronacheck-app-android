package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.navigation.fragment.navArgs
import nl.rijksoverheid.ctr.design.ExpandedBottomSheetDialogFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.DialogYourEventsResultSomethingWrongBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class YourEventsTestResultSomethingWrongDialogFragment : ExpandedBottomSheetDialogFragment() {

    private val args: YourEventsTestResultSomethingWrongDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return DialogYourEventsResultSomethingWrongBinding.inflate(inflater).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogYourEventsResultSomethingWrongBinding.bind(view)

        val type = args.protocolType
        val description =
            if (type is YourEventsFragmentType.RemoteProtocol3Type && type.originType is OriginType.Vaccination) {
                getString(R.string.dialog_vaccination_something_wrong_description)
            } else {
                getString(R.string.dialog_negative_test_result_something_wrong_description)
            }

        binding.description.setHtmlText(description, htmlLinksEnabled = true)

        binding.close.setOnClickListener {
            dismiss()
        }

        ViewCompat.setAccessibilityDelegate(binding.close, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View?,
                info: AccessibilityNodeInfoCompat?
            ) {
                info?.setTraversalBefore(binding.description)
                super.onInitializeAccessibilityNodeInfo(host, info)
            }
        })

    }
}
