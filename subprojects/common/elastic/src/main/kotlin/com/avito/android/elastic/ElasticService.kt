package com.avito.android.elastic

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface ElasticService {

    @Headers("Content-Type:application/json")
    @POST("{indexPattern}-{date}/_doc")
    fun log(
        @Path("indexPattern") indexPattern: String,
        @Path("date") date: String,
        @Body params: Map<String, String>
    ): Call<ResponseBody>
}
