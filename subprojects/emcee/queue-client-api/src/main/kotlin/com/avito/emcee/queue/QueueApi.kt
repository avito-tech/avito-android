package com.avito.emcee.queue

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

public interface QueueApi {

    @POST("queueVersion")
    public suspend fun queueVersion(): QueueVersion

    @POST("scheduleTests")
    public suspend fun scheduleTests(@Body body: ScheduleTestsBody): ScheduleTestsResponse

    @POST("jobState")
    public suspend fun jobState(@Body body: JobStatusBody): JobStateResponse

    @POST("jobResults")
    public suspend fun jobResults(@Body body: JobStatusBody): JobResultsResponse

    public companion object {

        public fun Retrofit.Builder.create(
            baseUrl: String,
            client: OkHttpClient
        ): QueueApi {
            return addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().build()
                ).failOnUnknown()
            )
                .client(client)
                .baseUrl(baseUrl)
                .build()
                .create()
        }
    }
}
