package com.avito.emcee.queue

import com.squareup.moshi.Moshi
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

    @POST("jobStatus")
    public suspend fun jobStatus(@Body body: JobStatusBody): JobStatus

    public companion object {

        public fun Retrofit.Builder.create(baseUrl: String): QueueApi {
            return addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder().build()
                ).failOnUnknown()
            )
                .baseUrl(baseUrl)
                .build()
                .create()
        }
    }
}
