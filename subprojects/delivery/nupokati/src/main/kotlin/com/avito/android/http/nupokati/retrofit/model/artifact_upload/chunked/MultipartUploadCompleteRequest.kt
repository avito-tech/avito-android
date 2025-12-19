@file:Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")

package com.avito.android.http.nupokati.retrofit.model.artifact_upload.chunked

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class MultipartUploadCompleteRequest(
    @Json(name = "platform")
    val platform: String,
    @Json(name = "project")
    val project: String,
    @Json(name = "version")
    val version: String,
    @Json(name = "buildNumber")
    val buildNumber: Int,
    @Json(name = "fileName")
    val fileName: String,
    @Json(name = "uploadId")
    val uploadId: String,
    @Json(name = "parts")
    val parts: List<MultipartUploadPart>,
)

@JsonClass(generateAdapter = true)
public data class MultipartUploadPart(
    @Json(name = "partNumber")
    val partNumber: Int,
    @Json(name = "etag")
    val etag: String,
)
