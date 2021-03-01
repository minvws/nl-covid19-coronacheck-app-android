package nl.rijksoverheid.ctr.holder.myoverview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import nl.rijksoverheid.ctr.holder.R
import nl.rijksoverheid.ctr.holder.databinding.FragmentDateOfBirthInputBinding
import nl.rijksoverheid.ctr.shared.ext.formatDate
import nl.rijksoverheid.ctr.shared.ext.fromHtml
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentDateOfBirthInputBinding.bind(view)
        binding.description.text = getString(R.string.date_of_birth_input_description).fromHtml()
        binding.dateOfBirthEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                presentDatePicker(binding)
            }
            true
        }
        binding.dateOfBirthEditText.addTextChangedListener {
            binding.button.isEnabled = it?.isNotEmpty() == true
        }
        binding.button.setOnClickListener {
            
        }
    }

    private fun presentDatePicker(binding: FragmentDateOfBirthInputBinding) {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
        datePickerBuilder.setCalendarConstraints(
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now()).build()
        )
        val datePicker = datePickerBuilder.build()

        datePicker.addOnPositiveButtonClickListener {
            binding.dateOfBirthEditText.setText(
                OffsetDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.of("UTC")).formatDate()
            )
        }
        datePicker.show(childFragmentManager, null)
    }

}
