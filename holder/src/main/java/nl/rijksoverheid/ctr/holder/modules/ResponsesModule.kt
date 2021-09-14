package nl.rijksoverheid.ctr.holder.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.OriginTypeJsonAdapter
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.RemoteCouplingStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.ui.create_qr.api.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.ui.create_qr.models.*
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import okhttp3.ResponseBody
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val responsesModule = module {
    single<Converter<ResponseBody, SignedResponseWithModel<RemoteProtocol>>>(named("SignedResponseWithModel")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            Types.newParameterizedType(
                SignedResponseWithModel::class.java,
                RemoteProtocol::class.java
            ), emptyArray()
        )
    }

    single<Converter<ResponseBody, CoronaCheckErrorResponse>>(named("ResponseError")) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            CoronaCheckErrorResponse::class.java, emptyArray()
        )
    }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(RemoteTestStatusJsonAdapter())
            .add(OriginTypeJsonAdapter())
            .add(RemoteCouplingStatusJsonAdapter())
            .add(
                PolymorphicJsonAdapterFactory.of(
                    RemoteProtocol::class.java, "protocolVersion"
                )
                    .withSubtype(RemoteTestResult2::class.java, "2.0")
                    .withSubtype(RemoteProtocol3::class.java, "3.0")
            )
            .add(
                PolymorphicJsonAdapterFactory.of(
                    RemoteEvent::class.java, "type"
                )
                    .withSubtype(RemoteEventPositiveTest::class.java, "positivetest")
                    .withSubtype(RemoteEventRecovery::class.java, "recovery")
                    .withSubtype(RemoteEventNegativeTest::class.java, "negativetest")
                    .withSubtype(RemoteEventVaccination::class.java, "vaccination")
            )
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}
