package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.holder.persistence.database.usecases.RemoveExpiredEventsUseCaseImpl
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import org.koin.dsl.module
import java.time.Clock

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val eventsUseCasesModule = module {
    factory<PaperProofCodeUseCase> {
        PaperProofCodeUseCaseImpl()
    }

    factory<GetEventProvidersWithTokensUseCase> {
        GetEventProvidersWithTokensUseCaseImpl(get())
    }
    factory<GetRemoteEventsUseCase> {
        GetRemoteEventsUseCaseImpl(get())
    }
    factory<QrCodeUseCase> {
        QrCodeUseCaseImpl(
            get(),
            get(),
            get()
        )
    }
    factory<GetEventsUseCase> { GetEventsUseCaseImpl(get(), get(), get(), get(), get(), get()) }
    factory<SaveEventsUseCase> { SaveEventsUseCaseImpl(get(), get()) }
    factory<ValidatePaperProofUseCase> {
        ValidatePaperProofUseCaseImpl(get(), get())
    }

    factory<GetEventsFromPaperProofQrUseCase> {
        GetEventsFromPaperProofQrUseCaseImpl(get(), get())
    }

    factory<RemoveExpiredEventsUseCase> {
        RemoveExpiredEventsUseCaseImpl(Clock.systemUTC(), get(), get())
    }
}
