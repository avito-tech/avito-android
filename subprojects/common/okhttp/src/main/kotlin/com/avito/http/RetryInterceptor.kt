package com.avito.http

import com.avito.logger.Logger
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * See also:
 * - [okhttp3.internal.http.RetryAndFollowUpInterceptor]
 */
class RetryInterceptor constructor(
    private val retries: Int = 5,
    private val allowedMethods: List<String> = listOf("GET"),
    private val allowedCodes: List<Int> = listOf(
        408, // client timeout
        500, // internal error
        502, // bad gateway
        503, // unavailable
        504  // gateway timeout
    ),
    private val delayMs: Long = TimeUnit.SECONDS.toMillis(1),
    private val useIncreasingDelay: Boolean = true,
    private val logger: Logger,
    private val onSuccess: (Response) -> Unit = {},
    private val onFailure: (Response) -> Unit = {}
) : Interceptor {

    init {
        require(retries >= 1)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var error: Throwable = IOException("Failed to execute request for unknown reason")

        var tryCount = 0
        while (response.shouldTry() && tryCount < retries) {
            tryCount++

            try {
                prepareForRetry(response)
                response = chain.proceed(request)
                logger.debug("Try: $tryCount. Response: $response")
            } catch (exception: IOException) {
                error = exception
                logger.critical("Try: $tryCount. Failed to execute request. Error: ${error.message}", exception)
            }

            if (response != null) {
                if (response.isSuccessful) {
                    onSuccess.invoke(response)
                } else {
                    onFailure.invoke(response)
                }
            }

            TimeUnit.MILLISECONDS.sleep(if (useIncreasingDelay) (tryCount * delayMs) else delayMs)
        }

        if (response == null) {
            throw error
        }
        return response
    }

    private fun prepareForRetry(response: Response?) {
        if (response != null && response.shouldTry()) {
            response.close()
        }
    }

    private fun Response?.shouldTry(): Boolean = when {
        this == null -> true
        else -> (request.method in allowedMethods) && (code in allowedCodes)
    }
}
