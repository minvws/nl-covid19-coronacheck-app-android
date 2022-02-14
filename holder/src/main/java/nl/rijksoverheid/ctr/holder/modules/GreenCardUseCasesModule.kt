package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.*
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val greenCardUseCasesModule = module {
    factory<CheckNewValidityInfoCardUseCase> {
        CheckNewValidityInfoCardUseCaseImpl(get(), get(), get())
    }
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
        GetDashboardItemsUseCaseImpl(get(), get(), get(), get(), get(), get(), get(), get(), get())
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
}
