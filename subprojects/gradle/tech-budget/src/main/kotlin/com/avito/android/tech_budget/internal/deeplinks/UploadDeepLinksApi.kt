package com.avito.android.tech_budget.internal.deeplinks

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.internal.deeplinks.models.UploadDeepLinksRequest
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.di.RetrofitProvider
import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.logger.LoggerFactory
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Provider

internal interface UploadDeepLinksApi {

    @POST("dumpDeepLinks/")
    fun dumpDeepLinks(@Body request: UploadDeepLinksRequest): Call<DumpResponse>

    companion object {
        fun create(
            baseUrl: String,
            ownerSerializer: Provider<OwnerSerializer>,
            loggerFactory: LoggerFactory
        ): UploadDeepLinksApi =
            RetrofitProvider(baseUrl, MoshiProvider(ownerSerializer), loggerFactory)
                .provide()
                .create()
    }
}
