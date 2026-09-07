package com.avito.android.tech_budget.internal.utils

import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

private const val MAX_ERROR_BODY_LENGTH = 1000

internal fun <T> Call<T>.executeWithHttpFailure(errorMessage: String): Response<T> {
    val response = try {
        execute()
    } catch (error: Throwable) {
        throw IllegalStateException(errorMessage, error)
    }

    if (!response.isSuccessful) {
        throw IllegalStateException("$errorMessage. ${response.describeFailure()}", HttpException(response))
    }

    return response
}

private fun Response<*>.describeFailure(): String {
    val body = errorBody()?.string().orEmpty().trim()

    return when {
        body.isEmpty() -> "HTTP ${code()}"
        body.length > MAX_ERROR_BODY_LENGTH -> "HTTP ${code()}: ${body.take(MAX_ERROR_BODY_LENGTH)}…"
        else -> "HTTP ${code()}: $body"
    }
}
