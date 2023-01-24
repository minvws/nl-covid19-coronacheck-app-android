/*
 *  Copyright (c) 2021 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 *
 */
package nl.rijksoverheid.ctr.appconfig.repositories

import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response

class ConfigHttpException(response: Response<JSONObject>) : HttpException(response)
