package com.avito.android.tech_budget.internal.feature_toggles

import com.avito.android.tech_budget.internal.dump.DumpResponse
import com.avito.android.tech_budget.internal.feature_toggles.models.UploadFeatureTogglesRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

internal interface UploadFeatureTogglesApi {

    @POST("dumpFeatureToggles/")
    fun dumpFeatureToggles(@Body request: UploadFeatureTogglesRequest): Call<DumpResponse>
}
