package com.avito.android.tech_budget.internal.owners.dependencies

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.owners.dependencies.models.UploadDependenciesRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadDependenciesApi {

    @POST("dumpModules/")
    fun dumpModules(@Body request: UploadDependenciesRequestBody): Call<DumpResponse>
}
