package com.avito.android.tech_budget.internal.dump

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class DumpResponse(
    @param:Json(name = "result") val result: Result
) {

    @JsonClass(generateAdapter = true)
    internal class Result(
        @param:Json(name = "id") val id: Int
    )
}
