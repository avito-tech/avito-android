package com.avito.alertino.internal

import com.avito.alertino.internal.model.SendNotificationBody
import com.avito.alertino.internal.model.SendNotificationResponse
import com.avito.alertino.internal.model.SendNotificationToThreadBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface AlertinoApi {

    @POST("sendNotificationV3")
    fun sendNotification(@Body body: SendNotificationBody): Call<SendNotificationResponse>

    @POST("sendNotificationToThreadV3")
    fun sendNotificationToThread(@Body body: SendNotificationToThreadBody): Call<SendNotificationResponse>
}
