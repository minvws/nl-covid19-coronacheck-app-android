package nl.rijksoverheid.ctr.shared.ext

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

/**
 * When you press multiple times on a button that navigates to a new fragment,
 * there can be a race condition where if you press multiple times
 * the fragment tries to navigate while the new fragment is already active.
 * Use this function if that can occur
 * @param currentId The current nav id of the view you are navigating from
 */
fun Fragment.findNavControllerSafety(currentId: Int): NavController? {
    try {
        val controller = NavHostFragment.findNavController(this)
        if (controller.currentDestination?.id != currentId) {
            return null
        }
        return controller
    } catch (e: Exception) {
        return null
    }
}

fun Fragment.findNavControllerSafety(): NavController? {
    return try {
        findNavController()
    } catch(e: Exception) {
        null
    }
}

fun Fragment.navigateSafety(directions: NavDirections) {
    try {
        findNavController().navigate(directions)
    } catch(e: Exception) {
        // no op
    }
}

fun Fragment.navigateSafety(currentId: Int, directions: NavDirections) {
    try {
        findNavControllerSafety(currentId)?.navigate(directions)
    } catch(e: Exception) {
        // no op
    }
}

fun Fragment.navigateSafety(currentId: Int, directionId: Int) {
    try {
        findNavControllerSafety(currentId)?.navigate(directionId)
    } catch(e: Exception) {
        // no op
    }
}

fun Fragment.showKeyboard(editText: EditText) {
    editText.requestFocus()
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}
