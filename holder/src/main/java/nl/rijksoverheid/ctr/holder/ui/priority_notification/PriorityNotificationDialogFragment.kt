package nl.rijksoverheid.ctr.holder.ui.priority_notification

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import nl.rijksoverheid.ctr.design.R
import nl.rijksoverheid.ctr.persistence.HolderCachedAppConfigUseCase
import org.koin.android.ext.android.inject

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

class PriorityNotificationDialogFragment : DialogFragment() {

    private val cachedAppConfigUseCase: HolderCachedAppConfigUseCase by inject()

    override fun onCreateDialog(savedInstanceState: Bundle?) = AlertDialog.Builder(requireContext())
    .setMessage("pou sia re")
    .setPositiveButton(getString(R.string.ok)) { _, _ -> }
    .create()
}
