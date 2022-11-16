package com.avito.android.tech_budget.internal.utils

import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response

internal fun <T> Call<T>.executeWithHttpFailure(errorMessage: String): Response<T> {
    try {
        val response = execute()
        if (!response.isSuccessful) throw HttpException(response)
        return response
    } catch (error: Throwable) {
        throw IllegalStateException(errorMessage, error)
    }
}
