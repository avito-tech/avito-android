package com.avito.emcee.worker

import com.avito.android.Result
import com.avito.emcee.moshi.SecondsToDurationAdapter
import com.avito.retrofit.adapter.ResultCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.addAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

public interface WorkerQueueApi {

    @POST("registerWorker")
    public suspend fun registerWorker(@Body body: RegisterWorkerBody): Result<RegisterWorkerResponse>

    @POST("updateWorkerDetails")
    public suspend fun updateWorkerDetails(@Body body: UpdateWorkerDetailsBody)

    @POST("getBucket")
    public suspend fun getBucket(@Body body: GetBucketBody): Result<GetBucketResponse>

    @POST("bucketResult")
    public suspend fun sendBucketResult(@Body body: SendBucketResultBody)

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun Retrofit.Builder.createWorkerQueueApi(client: OkHttpClient, baseUrl: String): WorkerQueueApi {
            val moshi = Moshi.Builder()
                .addAdapter(SecondsToDurationAdapter())
                .build()

            return addCallAdapterFactory(ResultCallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .baseUrl(baseUrl)
                .build()
                .create()
        }
    }
}
