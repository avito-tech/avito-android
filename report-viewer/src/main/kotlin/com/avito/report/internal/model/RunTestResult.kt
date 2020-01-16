package com.avito.report.internal.model

import com.google.gson.annotations.SerializedName

internal data class RunTestResult(
    @SerializedName("prepared_data") val preparedData: Map<String, Any>
)
