/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.data_migration

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import nl.rijksoverheid.ctr.api.json.Base64JsonAdapter
import nl.rijksoverheid.ctr.persistence.database.HolderDatabase

interface DataExportUseCase {
    suspend fun export(): List<String>
}

class DataExportUseCaseImpl(
    private val holderDatabase: HolderDatabase,
    private val moshi: Moshi,
    private val stringDataZipper: StringDataZipper
) : DataExportUseCase {
    override suspend fun export(): List<String> {
        val eventsGroups = holderDatabase.eventGroupDao().getAll().filter { !it.draft }
        val eventGroupParcels = eventsGroups.map {
            EventGroupParcel(
                expiryDate = it.expiryDate,
                providerIdentifier = it.providerIdentifier,
                type = it.type,
                jsonData = it.jsonData
            )
        }
        val parameterizedType =
            Types.newParameterizedType(List::class.java, EventGroupParcel::class.java)
        val adapter: JsonAdapter<List<EventGroupParcel>> = moshi.adapter(parameterizedType)
        val json = adapter.toJson(eventGroupParcels)

        val compressed = stringDataZipper.zip(json)

        val base64JsonAdapter = Base64JsonAdapter()
        val base64 = base64JsonAdapter.toBase64(compressed)

        val compChunks = base64.chunked(maxPackageSize)

        val migrationParcelAdapter = moshi.adapter(MigrationParcel::class.java)
        return compChunks.map {
            val parcel = MigrationParcel(
                index = compChunks.indexOf(it),
                numberOfPackages = compChunks.size,
                payload = it,
                version = version
            )
            base64JsonAdapter.toBase64(migrationParcelAdapter.toJson(parcel).toByteArray())
        }
    }

    companion object {
        const val version = "CC1"
        const val maxPackageSize = 800
    }
}
