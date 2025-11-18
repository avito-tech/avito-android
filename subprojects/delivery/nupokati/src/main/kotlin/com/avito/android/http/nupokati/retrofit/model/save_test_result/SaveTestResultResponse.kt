@file:Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")

package com.avito.android.http.nupokati.retrofit.model.save_test_result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SaveTestResultResponse(
    @Json(name = "result")
    val result: Result,
) {
    @JsonClass(generateAdapter = true)
    public data class Result(
        @Json(name = "success")
        val success: Boolean,
        @Json(name = "message")
        val message: String? = null,
    )
}
