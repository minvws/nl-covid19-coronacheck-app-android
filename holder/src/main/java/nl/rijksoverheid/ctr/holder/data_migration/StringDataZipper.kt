/*
 *  Copyright (c) 2023 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.holder.data_migration

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

interface StringDataZipper {
    fun zip(data: String): ByteArray
    fun unzip(data: ByteArray): String
}

class StringDataZipperImpl : StringDataZipper {
    override fun zip(data: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter(UTF_8).use { it.write(data) }
        return bos.toByteArray()
    }

    override fun unzip(data: ByteArray): String =
        GZIPInputStream(data.inputStream()).bufferedReader(UTF_8).use { it.readText() }
}
