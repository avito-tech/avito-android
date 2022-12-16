package com.avito.android.tech_budget.internal.ab_tests

import com.avito.android.tech_budget.internal.ab_tests.models.UploadABTestsRequest
import com.avito.android.tech_budget.internal.dump.DumpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadABTestsApi {

    @POST("dumpABTests/")
    fun dumpABTests(@Body request: UploadABTestsRequest): Call<DumpResponse>
}
