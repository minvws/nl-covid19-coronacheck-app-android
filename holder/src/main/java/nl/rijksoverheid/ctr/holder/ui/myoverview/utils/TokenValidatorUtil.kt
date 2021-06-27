package nl.rijksoverheid.ctr.holder.ui.myoverview.utils

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
interface TokenValidatorUtil {
    fun validate(token: String, checksum: String): Boolean
}

class TokenValidatorUtilImpl : TokenValidatorUtil {

    companion object {
        const val CODE_POINTS = "BCFGJLQRSTUVXYZ23456789"
    }

    /**
     * Validate a CoronaCheck token based on the Luhn algorithm (https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm)
     */
    override fun validate(token: String, checksum: String): Boolean {
        return try {
            val tokenWithChecksum = token + checksum[0]
            if (checksum.length == 2) {
                validateCheckCharacter(tokenWithChecksum)
            } else {
                false
            }
        } catch (e: StringIndexOutOfBoundsException) {
            false
        }
    }

    /**
     * Luhn algorithm (https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm
     */
    private fun validateCheckCharacter(input: String): Boolean {
        var factor = 1
        var sum = 0
        val n: Int = numberOfValidInputCharacters()

        // Starting from the right, work leftwards
        // Now, the initial "factor" will always be "1"
        // since the last character is the check character.

        // Starting from the right, work leftwards
        // Now, the initial "factor" will always be "1"
        // since the last character is the check character.
        for (i in input.length - 1 downTo 0) {
            val codePoint: Int = codePointFromCharacter(input[i])
            var addend = factor * codePoint

            // Alternate the "factor" that each "codePoint" is multiplied by
            factor = if (factor == 2) 1 else 2

            // Sum the digits of the "addend" as expressed in base "n"
            addend = addend / n + addend % n
            sum += addend
        }

        val remainder = sum % n

        return remainder == 0
    }

    private fun numberOfValidInputCharacters() = CODE_POINTS.length
    private fun codePointFromCharacter(char: Char) = CODE_POINTS.indexOf(char)
    private fun characterFromCodePoint(codePoint: Int) = CODE_POINTS[codePoint]
}
