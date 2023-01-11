package com.avito.retrofit.adapter

import com.avito.android.Result
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

public class ResultCallAdapterFactory private constructor() : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        if (returnType !is ParameterizedType) {
            error("Result return type must be parameterized as Call<Foo>")
        }

        val responseType = getParameterUpperBound(0, returnType)
        if (getRawType(responseType) != Result::class.java) return null
        if (responseType !is ParameterizedType) {
            error("Result return type must be parameterized as Result<Foo>")
        }

        val innerType = getParameterUpperBound(0, responseType)
        return ResultCallAdapter<Any>(innerType)
    }

    public companion object {
        public fun create(): ResultCallAdapterFactory = ResultCallAdapterFactory()
    }
}
