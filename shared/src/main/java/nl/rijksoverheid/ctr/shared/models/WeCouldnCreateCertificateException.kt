package nl.rijksoverheid.ctr.shared.models

import java.lang.Exception

/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */
class WeCouldnCreateCertificateException(val errorCode: String) : Exception("We couldn't create certificate exception")
