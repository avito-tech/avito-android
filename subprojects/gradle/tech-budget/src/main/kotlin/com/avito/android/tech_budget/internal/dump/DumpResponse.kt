package com.avito.android.tech_budget.internal.dump

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class DumpResponse(
    @Json(name = "result") val result: Result
) {

    @JsonClass(generateAdapter = true)
    internal class Result(
        @Json(name = "id") val id: String
    )
}
