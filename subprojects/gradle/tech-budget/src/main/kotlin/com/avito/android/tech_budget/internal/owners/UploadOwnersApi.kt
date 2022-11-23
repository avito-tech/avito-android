package com.avito.android.tech_budget.internal.owners

import com.avito.android.owner.adapter.OwnerAdapter
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.di.RetrofitProvider
import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.owners.models.UploadOwnersRequestBody
import com.avito.logger.LoggerFactory
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadOwnersApi {

    @POST("dumpOwners/")
    fun dumpOwners(@Body request: UploadOwnersRequestBody): Call<DumpResponse>

    companion object {
        fun create(baseUrl: String, uploadOwnersAdapter: OwnerAdapter, loggerFactory: LoggerFactory): UploadOwnersApi =
            RetrofitProvider(baseUrl, MoshiProvider(uploadOwnersAdapter), loggerFactory)
                .provide()
                .create()
    }
}
