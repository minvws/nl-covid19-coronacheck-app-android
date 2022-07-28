package nl.rijksoverheid.ctr.holder.modules

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import nl.rijksoverheid.ctr.holder.api.OriginTypeJsonAdapter
import nl.rijksoverheid.ctr.holder.api.RemoteCouplingStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.api.RemoteTestStatusJsonAdapter
import nl.rijksoverheid.ctr.holder.api.models.SignedResponseWithModel
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEvent
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventNegativeTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventPositiveTest
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventRecovery
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccination
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteEventVaccinationAssessment
import nl.rijksoverheid.ctr.holder.get_events.models.RemoteProtocol
import nl.rijksoverheid.ctr.holder.modules.qualifier.ErrorResponseQualifier
import nl.rijksoverheid.ctr.shared.models.CoronaCheckErrorResponse
import nl.rijksoverheid.ctr.shared.models.MijnCnErrorResponse
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

    single<Converter<ResponseBody, CoronaCheckErrorResponse>>(named(ErrorResponseQualifier.CORONA_CHECK)) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            CoronaCheckErrorResponse::class.java, emptyArray()
        )
    }

    single<Converter<ResponseBody, MijnCnErrorResponse>>(named(ErrorResponseQualifier.MIJN_CN)) {
        get<Retrofit>(Retrofit::class).responseBodyConverter(
            MijnCnErrorResponse::class.java, emptyArray()
        )
    }

    single {
        get<Moshi.Builder>(Moshi.Builder::class)
            .add(RemoteTestStatusJsonAdapter())
            .add(OriginTypeJsonAdapter())
            .add(RemoteCouplingStatusJsonAdapter())
            .add(
                PolymorphicJsonAdapterFactory.of(
                    RemoteEvent::class.java, "type"
                )
                    .withSubtype(RemoteEventVaccinationAssessment::class.java, "vaccinationassessment")
                    .withSubtype(RemoteEventPositiveTest::class.java, "positivetest")
                    .withSubtype(RemoteEventRecovery::class.java, "recovery")
                    .withSubtype(RemoteEventNegativeTest::class.java, "negativetest")
                    .withSubtype(RemoteEventVaccination::class.java, "vaccination")
            )
            .add(KotlinJsonAdapterFactory())
            .build()
    }
}
