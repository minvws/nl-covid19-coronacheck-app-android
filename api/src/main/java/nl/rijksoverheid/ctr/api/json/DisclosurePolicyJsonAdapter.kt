/*
 * Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 * Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 * SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.ctr.api.json

import com.squareup.moshi.*
import nl.rijksoverheid.ctr.shared.models.DisclosurePolicy

class DisclosurePolicyJsonAdapter : JsonAdapter<DisclosurePolicy>() {

    @FromJson
    override fun fromJson(reader: JsonReader): DisclosurePolicy {
        try {
            val results = mutableListOf<String>()
            reader.beginArray();
            while (reader.hasNext()) {
                results.add(reader.nextString())
            }
            reader.endArray()

            return when {
                results.contains("3G") && results.contains("1G") && results.size == 2 -> DisclosurePolicy.OneAndThreeG
                results.contains("1G") && results.size == 1 -> DisclosurePolicy.OneG
                results.contains("3G") && results.size == 1 -> DisclosurePolicy.ThreeG
                results.isEmpty() -> DisclosurePolicy.ZeroG
                else -> DisclosurePolicy.ZeroG
            }
        } catch (e: Exception) {
            return DisclosurePolicy.ZeroG
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: DisclosurePolicy?) {
        val results = value?.stringValue?.split(",") ?: listOf()

        writer.beginArray()
        results.forEach {
            writer.value(it)
        }
        writer.endArray()
    }
}