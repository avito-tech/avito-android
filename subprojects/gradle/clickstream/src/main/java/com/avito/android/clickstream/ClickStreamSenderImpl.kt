package com.avito.android.clickstream

import com.avito.android.Result
import com.avito.android.clickstream.api.ClickStreamApi
import com.avito.android.clickstream.api.ClickStreamEventRequest
import com.avito.android.clickstream.api.InfraClickStreamEvent
import com.avito.android.clickstream.api.InfraClickStreamEventRequest
import com.avito.android.clickstream.config.ClickStreamConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

public class ClickStreamSenderImpl(
    private val config: ClickStreamConfig,
) : ClickStreamSender {

    private val clickStreamApi: ClickStreamApi by lazy {
        Retrofit.Builder()
            .baseUrl(config.serviceUrl)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .client(buildClient())
            .build()
            .create(ClickStreamApi::class.java)
    }

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(config.readTimeOutInSeconds, TimeUnit.SECONDS)
            .connectTimeout(config.connectTimeOutInSeconds, TimeUnit.SECONDS)
            .build()
    }

    override fun sendEvents(envelope: ClickStreamEventRequest): Result<Unit> = Result.tryCatch {
        val call = if (config.useLegacyEndpoint) {
            clickStreamApi.sendEventsLegacy(envelope)
        } else {
            clickStreamApi.sendEvents(envelope.toInfra())
        }

        val response = call.execute()
        if (!response.isSuccessful) throw HttpException(response)
    }

    private fun ClickStreamEventRequest.toInfra(): InfraClickStreamEventRequest {
        return InfraClickStreamEventRequest(
            srcId = meta.srcId,
            events = events.map { event ->
                InfraClickStreamEvent(
                    eid = event.eventId,
                    version = event.version,
                    params = event.params,
                )
            }
        )
    }
}
