package nl.rijksoverheid.ctr.data.factory

import com.squareup.moshi.Moshi
import nl.rijksoverheid.ctr.data.api.TestApiClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class DependencyFactory {

    fun getTestApiClient(): TestApiClient {
        val retroFit = Retrofit.Builder()
            .baseUrl("https://api-ct.bananenhalen.nl")
            .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
            .build()
        return retroFit.create(TestApiClient::class.java)
    }

    fun getMoshi(): Moshi {
        return Moshi.Builder().build()
    }
}
