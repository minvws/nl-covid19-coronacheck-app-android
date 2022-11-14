package nl.rijksoverheid.ctr.holder.fuzzy_matching

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface SelectionDetailBottomSheetDescriptionUtil {
    fun get(
        selectionDetailData: List<SelectionDetailData>,
        separator: String,
        retrievedBy: (String) -> String
    ): String
}

class SelectionDetailBottomSheetDescriptionUtilImpl() : SelectionDetailBottomSheetDescriptionUtil {
    override fun get(
        selectionDetailData: List<SelectionDetailData>,
        separator: String,
        retrievedBy: (String) -> String
    ): String {
        val description = StringBuilder("<br/><br/>")

        selectionDetailData.forEach {
            description.append("<b>${it.type}</b><br/>")
            description.append("${retrievedBy(it.providerIdentifiers.joinToString(separator))} <br/>")
            description.append("${it.eventDate}<br/><br/>")
        }

        return description.toString()
    }
}
