package com.avito.android.tech_budget.internal.module_types

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.module_types.models.UploadModuleTypesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadModuleTypesApi {

    @POST("dumpModuleTypes/")
    fun dumpModuleTypes(@Body request: UploadModuleTypesRequest): Call<DumpResponse>
}
