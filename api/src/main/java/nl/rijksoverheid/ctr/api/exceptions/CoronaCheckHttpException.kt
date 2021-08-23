package nl.rijksoverheid.ctr.api.exceptions

import nl.rijksoverheid.ctr.api.models.CoronaCheckErrorResponse
import retrofit2.HttpException
import retrofit2.Response

class CoronaCheckHttpException(
    response: Response<*>,
    val responseError: CoronaCheckErrorResponse
) : HttpException(response)