package com.avito.android.tech_budget.internal.deeplinks

import com.avito.android.tech_budget.internal.deeplinks.models.UploadDeepLinksRequest
import com.avito.android.tech_budget.internal.dump.DumpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadDeepLinksApi {

    @POST("dumpDeepLinks/")
    fun dumpDeepLinks(@Body request: UploadDeepLinksRequest): Call<DumpResponse>
}
