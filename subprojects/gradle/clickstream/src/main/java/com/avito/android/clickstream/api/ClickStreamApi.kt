package com.avito.android.clickstream.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

public interface ClickStreamApi {

    @POST("1/json")
    public fun sendEventsLegacy(@Body envelope: ClickStreamEventRequest): Call<ResponseBody>

    @POST("0/json")
    public fun sendEvents(@Body request: InfraClickStreamEventRequest): Call<ResponseBody>
}
