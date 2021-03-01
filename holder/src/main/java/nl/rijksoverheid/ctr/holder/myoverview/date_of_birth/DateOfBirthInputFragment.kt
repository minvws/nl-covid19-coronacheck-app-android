package nl.rijksoverheid.ctr.holder.myoverview.date_of_birth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDateOfBirthInputBinding
import nl.rijksoverheid.ctr.shared.ext.formatDate
import nl.rijksoverheid.ctr.shared.ext.fromHtml
import nl.rijksoverheid.ctr.shared.livedata.EventObserver
import org.koin.androidx.viewmodel.ViewModelOwner
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.scope.emptyState
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DateOfBirthInputFragment : Fragment(R.layout.fragment_date_of_birth_input) {

    private val dateOfBirthInputViewModel: DateOfBirthInputViewModel by sharedViewModel(
        state = emptyState(),
        owner = {
            ViewModelOwner.from(
                findNavController().getViewModelStoreOwner(R.id.nav_date_of_birth),
                this
            )
        })

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDateOfBirthInputBinding.bind(view)
        binding.description.text = getString(R.string.date_of_birth_input_description).fromHtml()
        binding.dateOfBirthEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                presentDatePicker()
            }
            true
        }

        binding.dateOfBirthEditText.addTextChangedListener {
            binding.button.isEnabled = it?.isNotEmpty() == true
        }

        binding.button.setOnClickListener {
            findNavController().navigate(
                DateOfBirthInputFragmentDirections.actionDateOfBirthCheck()
            )
        }

        dateOfBirthInputViewModel.dateOfBirthMillisLiveData.observe(
            viewLifecycleOwner,
            EventObserver { millis ->
                binding.dateOfBirthEditText.setText(
                    OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.of("UTC"))
                        .formatDate()
                )
                binding.button.isEnabled = true
            })
    }

    private fun presentDatePicker(): MaterialDatePicker<Long> {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
        datePickerBuilder.setCalendarConstraints(
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()).build()
        )
        val datePicker = datePickerBuilder.build()
        datePicker.addOnPositiveButtonClickListener {
            dateOfBirthInputViewModel.setDateOfBirthMillis(it)
        }
        val isDatePickerShown =
            childFragmentManager.findFragmentByTag(MaterialDatePicker::class.simpleName) != null
        if (!isDatePickerShown) {
            datePicker.show(childFragmentManager, MaterialDatePicker::class.simpleName)
        }
        return datePickerBuilder.build()
    }

}
