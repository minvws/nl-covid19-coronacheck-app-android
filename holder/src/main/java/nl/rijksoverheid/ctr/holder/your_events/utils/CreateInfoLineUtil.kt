/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.your_events.utils

import androidx.core.text.HtmlCompat

abstract class CreateInfoLineUtil {
    fun createdLine(
        name: String,
        nameAnswer: String,
        isOptional: Boolean = false
    ): String {
        val sanitizedName = HtmlCompat.fromHtml(name, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        val sanitizedAnswer =
            HtmlCompat.fromHtml(nameAnswer, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
        return if (isOptional && nameAnswer.isEmpty()) "" else "$sanitizedName <b>$sanitizedAnswer</b><br/>"
    }
}
