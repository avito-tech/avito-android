package com.avito.android.tech_budget.internal.owners.dependencies

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.di.RetrofitProvider
import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.owners.dependencies.models.UploadDependenciesRequestBody
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadDependenciesApi {

    @POST("dumpModules/")
    fun dumpModules(@Body request: UploadDependenciesRequestBody): Call<DumpResponse>

    companion object {
        fun create(baseUrl: String, ownerSerializer: OwnerSerializer): UploadDependenciesApi =
            RetrofitProvider(baseUrl, MoshiProvider(DefaultOwnerAdapter(ownerSerializer)))
                .provide()
                .create()
    }
}
