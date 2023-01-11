package com.avito.retrofit.adapter

import com.avito.android.Result
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

public class ResultCallAdapter<T>(
    private val successType: Type
) : CallAdapter<T, Call<Result<T>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<Result<T>> = ResultCall(call, successType)
}
