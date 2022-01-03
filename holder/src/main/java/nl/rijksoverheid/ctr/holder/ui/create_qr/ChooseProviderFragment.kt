package nl.rijksoverheid.ctr.holder.ui.create_qr

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import nl.rijksoverheid.ctr.design.fragments.info.ButtonData
import nl.rijksoverheid.ctr.design.fragments.info.DescriptionData
import nl.rijksoverheid.ctr.design.fragments.info.InfoFragmentData
import nl.rijksoverheid.ctr.design.utils.InfoFragmentUtil
import nl.rijksoverheid.ctr.holder.HolderMainFragment
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentChooseProviderBinding
import nl.rijksoverheid.ctr.holder.persistence.database.entities.OriginType
import nl.rijksoverheid.ctr.shared.ext.navigateSafety
import nl.rijksoverheid.ctr.shared.utils.Accessibility.setAsAccessibilityButton
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class ChooseProviderFragment : Fragment(R.layout.fragment_choose_provider) {

    private val infoFragmentUtil: InfoFragmentUtil by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentChooseProviderBinding.bind(view)

        binding.providerCommercial.bind(
            R.string.choose_provider_commercial_title,
            null
        ) {
            findNavController().navigate(ChooseProviderFragmentDirections.actionCommercialTestCode(
                toolbarTitle = getString(R.string.commercial_test_type_code_toolbar_title),
                data = VerificationCodeFragmentData.CommercialTest,
            ))
        }

        binding.providerGgd.bind(
            R.string.choose_provider_ggd_title,
            null
        ) {
            navigateSafety(
                ChooseProviderFragmentDirections.actionGetEvents(
                    originType = OriginType.Test,
                    toolbarTitle = getString(R.string.choose_provider_toolbar)
                )
            )
        }

        binding.notYetTested.setOnClickListener {
            infoFragmentUtil.presentAsBottomSheet(
                childFragmentManager, InfoFragmentData.TitleDescriptionWithButton(
                    title = getString(R.string.not_yet_tested_title),
                    descriptionData = DescriptionData(R.string.not_yet_tested_description),
                    buttonData = ButtonData.LinkButton(
                        getString(R.string.not_yet_tested_button),
                        getString(R.string.url_make_appointment)
                    ),
                )
            )
        }

        binding.providerCommercial.root.setAsAccessibilityButton()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        (parentFragment?.parentFragment as HolderMainFragment).presentLoading(false)
    }
}