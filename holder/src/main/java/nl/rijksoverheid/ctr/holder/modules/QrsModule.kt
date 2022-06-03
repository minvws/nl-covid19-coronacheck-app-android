package nl.rijksoverheid.ctr.holder.modules

import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeAnimationUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodeAnimationUseCaseImpl
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCase
import nl.rijksoverheid.ctr.holder.qrcodes.usecases.QrCodesResultUseCaseImpl
import org.koin.dsl.module

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
val qrsModule = module {
    factory<QrCodesResultUseCase> {
        QrCodesResultUseCaseImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<QrCodeAnimationUseCase> { QrCodeAnimationUseCaseImpl(get()) }
}
