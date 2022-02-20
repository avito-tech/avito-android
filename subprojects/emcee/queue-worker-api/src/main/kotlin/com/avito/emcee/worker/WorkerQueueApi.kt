package com.avito.emcee.worker

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.POST

public interface WorkerQueueApi {

    @POST("registerWorker")
    public suspend fun registerWorker(body: RegisterWorkerBody): RegisterWorkerResponse

    @POST("getBucket")
    public suspend fun getBucket(body: GetBucketBody): GetBucketResponse

    @POST("bucketResult")
    public suspend fun sendBucketResult(body: SendBucketResultBody)

    public companion object {

        public fun Retrofit.Builder.create(baseUrl: String): WorkerQueueApi {
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
