package com.avito.retrofit.adapter

import com.avito.android.Result
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

internal class ResultCall<T>(
    private val delegate: Call<T>,
    private val successType: Type,
) : Call<Result<T>> {

    override fun enqueue(callback: Callback<Result<T>>) {
        delegate.enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@ResultCall, Response.success(response.toResult()))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ResultCall, Response.success(Result.Failure(t)))
            }
        })
    }

    override fun execute(): Response<Result<T>> {
        return Response.success(delegate.execute().toResult())
    }

    override fun clone(): Call<Result<T>> = ResultCall(delegate, successType)

    override fun isExecuted(): Boolean = delegate.isExecuted

    override fun cancel() = delegate.cancel()

    override fun isCanceled(): Boolean = delegate.isCanceled

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()

    private fun Response<T>.toResult(): Result<T> {
        if (!isSuccessful) {
            val errorBody = errorBody()?.string() ?: "empty"
            return Result.Failure(HttpException(code(), errorBody))
        }

        body()?.let { body -> return Result.Success(body) }

        // For example, in case of 204 No Content
        @Suppress("UNCHECKED_CAST")
        return when (successType) {
            Unit::class.java -> Result.Success(Unit) as Result<T>
            else -> Result.Failure(IllegalStateException("Response body was null"))
        }
    }
}
