package com.avito.emcee.queue

import com.avito.emcee.moshi.SecondsToDurationAdapter
import com.avito.emcee.queue.results.JobResultsResponse
import com.avito.emcee.queue.schedule.ScheduleTestsBody
import com.avito.emcee.queue.schedule.ScheduleTestsResponse
import com.avito.emcee.queue.status.JobStateResponse
import com.avito.emcee.queue.status.JobStatusBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

public interface QueueApi {

    @POST("scheduleTests")
    public suspend fun scheduleTests(@Body body: ScheduleTestsBody): ScheduleTestsResponse

    @POST("jobState")
    public suspend fun jobState(@Body body: JobStatusBody): JobStateResponse

    @POST("jobResults")
    public suspend fun jobResults(@Body body: JobStatusBody): JobResultsResponse

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun Retrofit.Builder.create(
            baseUrl: String,
            client: OkHttpClient
        ): QueueApi {
            return addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .addAdapter(SecondsToDurationAdapter())
                        .build()
                )
            )
                .client(client)
                .baseUrl(baseUrl)
                .build()
                .create()
        }
    }
}
