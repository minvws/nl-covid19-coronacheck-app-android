package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.persistence.database.usecases.*
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetDashboardItemsUseCase
import nl.rijksoverheid.ctr.holder.ui.create_qr.usecases.GetDashboardItemsUseCaseImpl
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
        GetDashboardItemsUseCaseImpl(get(), get(), get(), get(), get())
    }
}
