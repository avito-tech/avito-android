package com.avito.reportviewer.internal.model

import com.google.gson.annotations.SerializedName

internal data class PreparedData(
    @SerializedName("verdict") val verdict: String?,
    @SerializedName("run_duration") val runDuration: Int,
    @SerializedName("error_hash") val errorHash: String,
    @SerializedName("tc_build") val tcBuild: String?,
    @SerializedName("skip_reason") val skipReason: String?,
    @SerializedName("external_id") val externalId: String?,
    @SerializedName("testcase") val ctulhuTestCase: CtulhuTestCase?,
    @SerializedName("features") val features: List<String>?,
    @SerializedName("tag_id") val tagId: List<Int>?,
    @SerializedName("feature_id") val featureIds: List<Int>?,
    @SerializedName("priority_id") val priorityId: Int?,
    @SerializedName("behavior_id") val behaviorId: Int?,
    @SerializedName("e2e") val e2e: Boolean?,
    @SerializedName("is_flaky") val isFlaky: Boolean?,
    @SerializedName("flaky_reason") val flakyReason: String?
)
