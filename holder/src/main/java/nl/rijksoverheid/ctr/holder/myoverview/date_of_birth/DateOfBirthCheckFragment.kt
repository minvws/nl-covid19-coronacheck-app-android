package nl.rijksoverheid.ctr.holder.myoverview.date_of_birth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDateOfBirthCheckBinding
import nl.rijksoverheid.ctr.shared.ext.formatDate
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.scope.emptyState
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DateOfBirthCheckFragment : Fragment(R.layout.fragment_date_of_birth_check) {

    private val dateOfBirthInputViewModel: DateOfBirthInputViewModel by sharedViewModel(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_date_of_birth),
                this
            )
        })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDateOfBirthCheckBinding.bind(view)

        val dateOfBirth = dateOfBirthInputViewModel.retrievedDateOfBirthMillis
        if (dateOfBirth == null) {
            findNavController().navigate(DateOfBirthCheckFragmentDirections.actionMyOverview())
        } else {
            val dateOfBirthString = OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(dateOfBirth),
                ZoneOffset.UTC
            ).formatDate()

            binding.description.text =
                getString(R.string.date_of_birth_check_description, dateOfBirthString).fromHtml()
            binding.checkbox.text =
                getString(R.string.date_of_birth_check_confirmation, dateOfBirthString).fromHtml()

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                binding.buttonNext.isEnabled = isChecked
            }

            binding.buttonNext.setOnClickListener {
                dateOfBirthInputViewModel.saveDateOfBirth()
                MaterialAlertDialogBuilder(requireContext())
                    .setView(R.layout.dialog_date_of_birth_saved)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        findNavController().navigate(DateOfBirthCheckFragmentDirections.actionCommercialTestType())
                    }
                    .show()
            }

            binding.buttonEdit.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}
