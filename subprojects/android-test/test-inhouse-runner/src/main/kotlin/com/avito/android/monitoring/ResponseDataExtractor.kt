package com.avito.android.monitoring

import com.avito.http.toPlainText
import okhttp3.Response
import okhttp3.ResponseBody

object ResponseDataExtractor {

    fun extract(response: Response): RequestResponseData {
        return RequestResponseData(
            requestUrl = response.request().url().toString(),
            host = response.request().url().host(),
            requestBody = response.request().body()?.toPlainText(),
            responseCode = response.code(),
            responseBody = response.body()?.extractBody()
        )
    }

    private fun ResponseBody.extractBody(): String {
        val source = source()
        source.request(java.lang.Long.MAX_VALUE) // Buffer the entire body.
        return source.buffer.clone().readString(Charsets.UTF_8)
    }
}
