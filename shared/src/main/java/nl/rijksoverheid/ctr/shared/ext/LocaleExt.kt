package nl.rijksoverheid.ctr.shared.ext

import java.util.*

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val Locale.flagEmoji: String
    get() {
        val firstLetter = Character.codePointAt(country, 0) - 0x41 + 0x1F1E6
        val secondLetter = Character.codePointAt(country, 1) - 0x41 + 0x1F1E6

        // check if the locale created by the country code provided
        // is valid. if it is not, we leave it empty
        return try {
            isO3Country
            String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
        } catch (exception: MissingResourceException) {
            ""
        }
    }