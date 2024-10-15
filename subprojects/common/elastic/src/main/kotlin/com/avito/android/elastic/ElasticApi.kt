package com.avito.android.elastic

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface ElasticApi {

    @Headers("Content-Type:application/json")
    @POST("{indexName}-{indexDate}/_doc")
    fun sendDocument(
        @Header("Authorization") authApiKeyHeaderValue: String?,
        @Path("indexName") indexPattern: String,
        /**
         * Best practise
         * Using for search optimization. Each day new index. Simplifies search through one day
         */
        @Path("indexDate") date: String,
        @Body params: Map<String, String>
    ): Call<ResponseBody>
}
