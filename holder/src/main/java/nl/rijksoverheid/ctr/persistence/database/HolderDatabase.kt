package nl.rijksoverheid.ctr.persistence.database

import android.content.Context
import androidx.room.RoomDatabase
import java.io.File

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

abstract class HolderDatabase : RoomDatabase() {

    companion object {
        fun deleteDatabase(
            context: Context
        ) {
            try {
                val file = File(context.filesDir.parentFile, "databases/holder-database")
                file.delete()
            } catch (e: Exception) {
                // no op
            }
        }
    }
}
