package com.avito.http

import com.avito.logger.Logger
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * See also:
 * - [okhttp3.internal.http.RetryAndFollowUpInterceptor]
 */
public class RetryInterceptor constructor(
    private val retries: Int = 5,
    private val allowedMethods: List<String> = listOf("GET"),
    private val allowedCodes: List<Int> = listOf(
        HttpCodes.CLIENT_TIMEOUT,
        HttpCodes.INTERNAL_ERROR,
        HttpCodes.BAD_GATEWAY,
        HttpCodes.UNAVAILABLE,
        HttpCodes.GATEWAY_TIMEOUT
    ),
    private val delayMs: Long = TimeUnit.SECONDS.toMillis(1),
    private val useIncreasingDelay: Boolean = true,
    private val logger: Logger,
    private val onSuccess: (Response) -> Unit = {},
    private val onFailure: (Response) -> Unit = {},
    private val modifyRetryRequest: (Request) -> Request = { it }
) : Interceptor {

    init {
        require(retries >= 1)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var response: Response? = null
        var error: Throwable = IOException("Failed to execute request for unknown reason")

        var tryCount = 0
        while (response.shouldTry() && tryCount < retries) {
            tryCount++

            try {
                prepareForRetry(response)
                val request = if (tryCount > 1) {
                    val modifiedRequest = modifyRetryRequest(originalRequest)
                    logger.debug("Retrying request ($tryCount / $retries) to ${modifiedRequest.url.redact()}")
                    modifiedRequest
                } else {
                    originalRequest
                }
                response = chain.proceed(request)
            } catch (exception: IOException) {
                error = exception
                logger.warn("Failed to execute request. Error: ${error.message}", exception)
            }

            if (response != null) {
                if (response.isSuccessful) {
                    onSuccess.invoke(response)
                } else {
                    onFailure.invoke(response)
                }
            }

            TimeUnit.MILLISECONDS.sleep(if (useIncreasingDelay) tryCount * delayMs else delayMs)
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
        else -> request.method in allowedMethods && code in allowedCodes
    }
}
