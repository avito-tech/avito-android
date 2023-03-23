package com.avito.android.tech_budget.internal.module_dependencies

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.module_dependencies.models.UploadModuleDependenciesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadModuleDependenciesApi {

    @POST("dumpModuleDependencies/")
    fun dumpModuleDependencies(@Body request: UploadModuleDependenciesRequest): Call<DumpResponse>
}
