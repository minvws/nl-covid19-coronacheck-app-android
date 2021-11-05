/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.ctr.qrscanner

import nl.rijksoverheid.ctr.zebrascanner.ZebraManager
import nl.rijksoverheid.ctr.zebrascanner.ZebraManagerImpl
import org.koin.dsl.module

val qrScannerModule = module {
    factory<QrCodeProcessor> { QrCodeProcessorImpl() }
    factory<ZebraManager> { ZebraManagerImpl(get()) }
}