package com.avito.emcee.worker

import retrofit2.http.POST

public interface WorkerQueueApi {

    @POST("registerWorker")
    public suspend fun registerWorker(body: RegisterWorkerBody): RegisterWorkerResponse

    @POST("getBucket")
    public suspend fun getBucket(): GetBucketResponse

    @POST("bucketResult")
    public suspend fun sendBucketResult(body: SendBucketResultBody)
}
