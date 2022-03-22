package nl.rijksoverheid.ctr.appconfig

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import nl.rijksoverheid.ctr.appconfig.databinding.FragmentNewFeatureItemBinding
import nl.rijksoverheid.ctr.appconfig.models.NewFeatureItem
import nl.rijksoverheid.ctr.shared.utils.AndroidUtil
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class NewFeatureItemFragment : Fragment(R.layout.fragment_new_feature_item) {

    companion object {
        private const val EXTRA_NEW_FEATURE_ITEM = "EXTRA_NEW_FEATURE_ITEM"

        fun getInstance(newFeatureItem: NewFeatureItem): NewFeatureItemFragment {
            val fragment =
                NewFeatureItemFragment()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_NEW_FEATURE_ITEM, newFeatureItem)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val androidUtil: AndroidUtil by inject()

    private val item: NewFeatureItem by lazy {
        arguments?.getParcelable<NewFeatureItem>(
            EXTRA_NEW_FEATURE_ITEM
        ) ?: throw Exception("Failed to get item")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentNewFeatureItemBinding.bind(view)

        binding.title.text = getString(item.titleResource)
        binding.subTitle.text = getString(item.subtitleResource)
        item.subTitleColor?.let {
            binding.subTitle.setTextColor(ContextCompat.getColor(requireContext(), it))
        }
        binding.description.setHtmlText(getString(item.description), false)

        if (androidUtil.isSmallScreen()) {
            binding.image.visibility = View.GONE
        } else {
            binding.image.visibility = View.VISIBLE
            if (item.imageResource != 0) {
                binding.image.setImageResource(item.imageResource)
            }
            item.backgroundColor?.let {
                binding.image.setBackgroundColor(ContextCompat.getColor(requireContext(), it))
            }
        }
    }
}
