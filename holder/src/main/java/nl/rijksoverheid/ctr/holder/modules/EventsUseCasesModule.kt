package nl.rijksoverheid.ctr.holder.modules

import java.time.Clock
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogUseCase
import nl.rijksoverheid.ctr.holder.dashboard.usecases.ShowBlockedEventsDialogUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventProvidersWithTokensUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventProvidersWithTokensUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventsUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetEventsUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetMijnCnEventsUsecase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetMijnCnEventsUsecaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteEventsUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteEventsUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.GetRemoteProtocolFromEventGroupUseCaseImpl
import nl.rijksoverheid.ctr.holder.get_events.usecases.PersistBlockedEventsUseCase
import nl.rijksoverheid.ctr.holder.get_events.usecases.PersistBlockedEventsUseCaseImpl
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetEventsFromPaperProofQrUseCaseImpl
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticInputCodeUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticInputCodeUseCaseImpl
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.ValidatePaperProofDomesticUseCaseImpl
import nl.rijksoverheid.ctr.holder.pdf.PreviewPdfUseCase
import nl.rijksoverheid.ctr.holder.pdf.PreviewPdfUseCaseImpl
import nl.rijksoverheid.ctr.holder.pdf.PrintExportDccUseCase
import nl.rijksoverheid.ctr.holder.pdf.PrintExportDccUseCaseImpl
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeUseCaseImpl
import nl.rijksoverheid.ctr.holder.saved_events.usecases.GetSavedEventsUseCase
import nl.rijksoverheid.ctr.holder.saved_events.usecases.GetSavedEventsUseCaseImpl
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCase
import nl.rijksoverheid.ctr.holder.your_events.usecases.SaveEventsUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.usecases.DraftEventUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.DraftEventUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoveExpiredEventsUseCase
import nl.rijksoverheid.ctr.persistence.database.usecases.RemoveExpiredEventsUseCaseImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val eventsUseCasesModule = module {
    factory<ValidatePaperProofDomesticInputCodeUseCase> {
        ValidatePaperProofDomesticInputCodeUseCaseImpl()
    }

    factory<ValidatePaperProofDomesticUseCase> {
        ValidatePaperProofDomesticUseCaseImpl(get(), get(), get())
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
            get(),
            get()
        )
    }
    factory<GetEventsUseCase> { GetEventsUseCaseImpl(get(), get(), get(), get()) }
    factory<GetMijnCnEventsUsecase> { GetMijnCnEventsUsecaseImpl(get(), get(), get()) }
    factory<SaveEventsUseCase> { SaveEventsUseCaseImpl(get(), get(), get(), get(), get()) }

    factory<GetEventsFromPaperProofQrUseCase> {
        GetEventsFromPaperProofQrUseCaseImpl(get(), get())
    }

    factory<RemoveExpiredEventsUseCase> {
        RemoveExpiredEventsUseCaseImpl(Clock.systemUTC(), get(), get())
    }
    factory<GetSavedEventsUseCase> {
        GetSavedEventsUseCaseImpl(get(), get(), get(), get(), get(), get(), get())
    }
    factory<GetRemoteProtocolFromEventGroupUseCase> {
        GetRemoteProtocolFromEventGroupUseCaseImpl(get(), get())
    }
    factory<PersistBlockedEventsUseCase> { PersistBlockedEventsUseCaseImpl(get()) }
    factory<ShowBlockedEventsDialogUseCase> { ShowBlockedEventsDialogUseCaseImpl(get()) }
    factory<DraftEventUseCase> { DraftEventUseCaseImpl(get()) }
    factory<PrintExportDccUseCase> { PrintExportDccUseCaseImpl(get(), get(), get()) }
    factory<PreviewPdfUseCase> { PreviewPdfUseCaseImpl() }
}
