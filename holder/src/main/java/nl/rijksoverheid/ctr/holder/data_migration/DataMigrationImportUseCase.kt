/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.holder.data_migration

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import timber.log.Timber

interface DataMigrationImportUseCase {
    suspend fun import(content: String): MigrationParcel?
    suspend fun merge(migrationParcels: List<MigrationParcel>): List<EventGroupParcel>
}

class DataMigrationImportUseCaseImpl(
    private val moshi: Moshi,
    private val stringDataZipper: StringDataZipper
) : DataMigrationImportUseCase {

    private val base64JsonAdapter = Base64JsonAdapter()

    override suspend fun import(content: String): MigrationParcel? {
        val decodedContent = base64JsonAdapter.fromBase64(content)

        val migrationParcelAdapter = moshi.adapter(MigrationParcel::class.java)

        return migrationParcelAdapter.fromJson(String(decodedContent))
    }

    override suspend fun merge(migrationParcels: List<MigrationParcel>): List<EventGroupParcel> {
        val mergedMigrationParcel =
            migrationParcels.sortedBy { it.index }.map { base64JsonAdapter.fromBase64(it.payload) }
                .reduce { acc, bytes -> acc + bytes }

        val uncompressed = stringDataZipper.unzip(mergedMigrationParcel)

        Timber.tag("migration").d(uncompressed)

        val parameterizedType =
            Types.newParameterizedType(List::class.java, EventGroupParcel::class.java)
        val adapter: JsonAdapter<List<EventGroupParcel>> = moshi.adapter(parameterizedType)

        val eventGroupParcels = adapter.fromJson(uncompressed)

        return eventGroupParcels ?: emptyList()
    }
}
