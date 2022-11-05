package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.internal.warnings.upload.model.WarningsRequestBody
import retrofit2.HttpException

internal class WarningsSender(private val api: UploadWarningsApi) {

    constructor(baseUrl: String) : this(UploadWarningsApi.create(baseUrl))

    fun send(warnings: List<Warning>, dumpInfo: DumpInfo) {
        try {
            val response = api.dumpWarnings(WarningsRequestBody(dumpInfo, warnings)).execute()
            if (!response.isSuccessful) throw HttpException(response)
        } catch (exception: Throwable) {
            throw IllegalStateException("Upload warnings request failed", exception)
        }
    }
}
