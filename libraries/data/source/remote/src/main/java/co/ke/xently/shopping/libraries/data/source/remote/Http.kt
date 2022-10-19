package co.ke.xently.shopping.libraries.data.source.remote

import retrofit2.Response

object Http {
    @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
    suspend fun <T, E : HttpException> sendRequest(
        errorClass: Class<E>,
        request: suspend () -> Response<T>,
    ): Result<T> {
        val response = request.invoke() // Initiate actual network request call
        val (statusCode, body, errorBody) = Triple(
            response.code(),
            response.body(),
            response.errorBody()
        )
        return if (response.isSuccessful) {
            if (statusCode == 204) {
                throw HttpException("No results",
                    errorCode = HttpException.ERROR_CODE_EMPTY_RESPONSE,
                    statusCode = 204)
            } else {
                Result.success(body ?: Any() as T)
            }
        } else {
            throw try {
                Serialization.JSON_CONVERTER.fromJson(
                    // The following is potentially blocking! Assume the consumer will call the
                    // suspend function from IO dispatcher.
                    errorBody!!.string(),
                    errorClass
                )
            } catch (ex: IllegalStateException) {
                HttpException(response.message())
            }.apply {
                if (this.statusCode == null) {
                    this.statusCode = statusCode
                }
            }
        }
    }

    suspend fun <T> sendRequest(request: suspend () -> Response<T>) =
        sendRequest(errorClass = HttpException::class.java, request = request)
}