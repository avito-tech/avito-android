package com.avito.android.tech_budget.internal.perf_screen_owners

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.perf_screen_owners.models.UploadPerfScreenOwnersRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadPerfScreenOwnersApi {

    @POST("dumpPerformanceScreenInfos/")
    fun dumpPerfOwners(@Body req: UploadPerfScreenOwnersRequest): Call<DumpResponse>
}
