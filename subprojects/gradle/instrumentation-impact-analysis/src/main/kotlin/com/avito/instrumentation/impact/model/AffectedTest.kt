package com.avito.instrumentation.impact.model

import com.google.gson.annotations.SerializedName

internal data class AffectedTest(
    @SerializedName("className")
    val className: String,
    @SerializedName("affectionType")
    val affectionType: AffectionType,
    @SerializedName("methods")
    val methods: Set<String>
)
