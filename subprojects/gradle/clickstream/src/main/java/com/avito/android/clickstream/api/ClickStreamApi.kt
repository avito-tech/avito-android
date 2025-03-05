package com.avito.android.clickstream.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

public interface ClickStreamApi {

    @POST("1/json")
    public fun sendEvents(@Body envelope: ClickStreamEventRequest): Call<ResponseBody>
}
