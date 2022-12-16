package com.avito.android.tech_budget.internal.owners

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.owners.models.UploadOwnersRequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadOwnersApi {

    @POST("dumpOwners/")
    fun dumpOwners(@Body request: UploadOwnersRequestBody): Call<DumpResponse>
}
