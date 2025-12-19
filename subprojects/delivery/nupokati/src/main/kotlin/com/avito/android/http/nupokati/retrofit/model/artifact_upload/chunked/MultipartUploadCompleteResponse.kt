@file:Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")

package com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class MultipartUploadCompleteResponse(
    @Json(name = "result")
    val result: Result,
) {
    @JsonClass(generateAdapter = true)
    public data class Result(
        @Json(name = "uri")
        val uri: String? = null,
        @Json(name = "error")
        val error: String? = null,
    )
}
