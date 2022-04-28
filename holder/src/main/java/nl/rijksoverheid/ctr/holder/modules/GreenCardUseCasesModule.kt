package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.dashboard.usecases.RemoveExpiredGreenCardsUseCase
import nl.rijksoverheid.ctr.dashboard.usecases.RemoveExpiredGreenCardsUseCaseImpl
import nl.rijksoverheid.ctr.holder.dashboard.usecases.*
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetPaperProofTypeUseCase
import nl.rijksoverheid.ctr.holder.paper_proof.usecases.GetPaperProofTypeUseCaseImpl
import nl.rijksoverheid.ctr.persistence.database.usecases.*
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val greenCardUseCasesModule = module {
    factory<GetRemoteGreenCardsUseCase> {
        GetRemoteGreenCardsUseCaseImpl(get(), get(), get())
    }
    factory<SyncRemoteGreenCardsUseCase> {
        SyncRemoteGreenCardsUseCaseImpl(get(), get(), get(), get())
    }
    factory<CreateDomesticGreenCardUseCase> {
        CreateDomesticGreenCardUseCaseImpl(get())
    }
    factory<CreateEuGreenCardUseCase> {
        CreateEuGreenCardUseCaseImpl(get(), get())
    }
    factory<GetDashboardItemsUseCase> {
        GetDashboardItemsUseCaseImpl(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    factory<SplitDomesticGreenCardsUseCase> {
        SplitDomesticGreenCardsUseCaseImpl(get(), get())
    }
    factory<SortGreenCardItemsUseCase> {
        SortGreenCardItemsUseCaseImpl(get(), get())
    }
    factory<RemoveExpiredGreenCardsUseCase> {
        RemoveExpiredGreenCardsUseCaseImpl(get())
    }
    factory<GetPaperProofTypeUseCase> {
        GetPaperProofTypeUseCaseImpl(get(), get(), get())
    }
    factory<ShowCoronaMelderItemUseCase> {
        ShowCoronaMelderItemUseCaseImpl(get(), get())
    }
}
