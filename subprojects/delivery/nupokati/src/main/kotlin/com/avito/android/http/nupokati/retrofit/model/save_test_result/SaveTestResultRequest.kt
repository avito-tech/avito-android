@file:Suppress("ANNOTATION_WILL_BE_APPLIED_ALSO_TO_PROPERTY_OR_FIELD")

package com.avito.android.http.nupokati.retrofit.model.save_test_result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class SaveTestResultRequest(
    @Json(name = "project")
    val project: String,
    @Json(name = "platform")
    val platform: String,
    @Json(name = "buildNumber")
    val buildNumber: Int,
    @Json(name = "reportUrl")
    val reportUrl: String,
    @Json(name = "reportCoordinates")
    val reportCoordinates: ReportCoordinates,
) {
    @JsonClass(generateAdapter = true)
    public data class ReportCoordinates(
        @Json(name = "planSlug")
        val planSlug: String,
        @Json(name = "jobSlug")
        val jobSlug: String,
        @Json(name = "runId")
        val runId: String,
    )
}
