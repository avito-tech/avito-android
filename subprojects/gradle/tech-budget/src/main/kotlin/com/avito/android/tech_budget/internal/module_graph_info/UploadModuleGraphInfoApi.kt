package com.avito.android.tech_budget.internal.module_graph_info

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.module_graph_info.models.UploadModuleGraphInfoRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadModuleGraphInfoApi {

    @POST("dumpModuleGraphInfo/")
    fun dumpModuleGraphInfo(@Body request: UploadModuleGraphInfoRequest): Call<DumpResponse>
}
