package com.avito.android.tech_budget.internal.compilation_info

import com.avito.android.tech_budget.internal.compilation_info.models.UploadModulesCompilationInfoRequest
import com.avito.android.tech_budget.internal.dump.DumpResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadModulesCompilationInfoApi {

    @POST("dumpModuleCompilations/")
    fun dumpModulesInfo(@Body request: UploadModulesCompilationInfoRequest): Call<DumpResponse>
}
