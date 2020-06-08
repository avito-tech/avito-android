package com.avito.filestorage

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

internal class ToStringConverterFactory : Converter.Factory() {

    private val uriRegex = Regex("uri\":.*\"(.+)\"")
    private val mediaType = "text/html".toMediaType()

    override fun responseBodyConverter(
        type: Type?,
        annotations: Array<Annotation>?,
        retrofit: Retrofit?
    ): Converter<ResponseBody, *>? {
        return if (String::class.java == type) {
            Converter<ResponseBody, String> { value -> uriRegex.find(value.string())?.groupValues?.getOrNull(1) }
        } else null
    }

    override fun requestBodyConverter(
        type: Type?,
        parameterAnnotations: Array<out Annotation>?,
        methodAnnotations: Array<out Annotation>?,
        retrofit: Retrofit?
    ): Converter<*, RequestBody>? {
        return if (String::class.java == type) {
            Converter<String, RequestBody> { value -> value.toRequestBody(mediaType) }
        } else null
    }
}
