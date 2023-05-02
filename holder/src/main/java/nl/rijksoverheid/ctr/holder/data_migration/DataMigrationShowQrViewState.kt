/*
 * Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.holder.data_migration

import android.graphics.Bitmap
import nl.rijksoverheid.ctr.shared.models.ErrorResult

sealed class DataMigrationShowQrViewState {
    data class ShowQrs(val bitmaps: List<Bitmap>) : DataMigrationShowQrViewState()
    data class ShowError(val errorResults: List<ErrorResult>) : DataMigrationShowQrViewState()
}
