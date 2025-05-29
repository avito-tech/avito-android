package com.avito.android.tech_budget.internal.module_graph_info

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.module_graph_info.app_dependencies.UploadModuleGraphAppDependenciesRequest
import com.avito.android.tech_budget.internal.module_graph_info.dependencies.UploadModuleGraphDependenciesRequest
import com.avito.android.tech_budget.internal.module_graph_info.sizes.UploadModuleSizesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadModuleGraphInfoApi {

    @POST("dumpModuleGraphAppDependencies/")
    fun dumpModuleGraphAppDependencies(@Body request: UploadModuleGraphAppDependenciesRequest): Call<DumpResponse>

    @POST("dumpModuleGraphDependencies/")
    fun dumpModuleGraphDependencies(@Body request: UploadModuleGraphDependenciesRequest): Call<DumpResponse>

    @POST("dumpModuleSizes/")
    fun dumpModuleSizes(@Body request: UploadModuleSizesRequest): Call<DumpResponse>
}
