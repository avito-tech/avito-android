package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.internal.di.MoshiProvider
import com.avito.android.tech_budget.internal.di.RetrofitProvider
import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.warnings.upload.model.WarningsRequestBody
import retrofit2.Call
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadWarningsApi {

    @POST("/dumpWarnings")
    fun dumpWarnings(@Body request: WarningsRequestBody): Call<DumpResponse>

    companion object {
        fun create(baseUrl: String, ownerSerializer: OwnerSerializer): UploadWarningsApi =
            RetrofitProvider(baseUrl, MoshiProvider { ownerSerializer })
                .provide()
                .create()
    }
}
