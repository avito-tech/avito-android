package com.avito.http

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * Если оригинальный запрос получил в ответ один из кодов [doFallbackOnTheseCodes],
 * трансформируем запрос [fallbackRequest] и пробуем еще раз,
 * сообщаем о том что пришлось использовать fallback при помощи [onFallback]
 */
class FallbackInterceptor(
    private val fallbackRequest: (Request) -> Request,
    private val doFallbackOnTheseCodes: List<Int> = listOf(
        HttpCodes.NOT_FOUND,
        HttpCodes.UNAVAILABLE,
        HttpCodes.BAD_GATEWAY,
        HttpCodes.GATEWAY_TIMEOUT
    )
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        var exception: IOException? = null

        val response = try {
            chain.proceed(request)
        } catch (e: IOException) {
            exception = e
            null
        }

        return if (isFallbackNeeded(response)) {
            // возможно помогает от Transmitter.prepareToConnect IllegalStateException, не удалось воспроизвести в тесте
            response?.close()
            chain.doFallback(request)
        } else {
            response ?: throw exception!!
        }
    }

    private fun isFallbackNeeded(response: Response?): Boolean {
        return when {
            response == null -> true
            response.isSuccessful -> false
            else -> response.code in doFallbackOnTheseCodes
        }
    }

    private fun Interceptor.Chain.doFallback(originalRequest: Request): Response =
        proceed(fallbackRequest.invoke(originalRequest))
}
