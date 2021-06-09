package com.avito.report.internal.model

import com.google.gson.annotations.SerializedName

/**
 * большинство полей здесь nullable, но это не приведено в соответствие с обязательностью в testreport,
 * gson не падает здесь, поэтому обрабатываем на маппинге выше уровнем см. [SimpleRunTest]
 */
internal data class ListResult(
    @SerializedName("id") val id: String,
    @SerializedName("test_case_id") val testCaseId: String?,
    @SerializedName("test_name") val testName: String,
    @SerializedName("data_set_number") val dataSetNumber: Int?,
    @SerializedName("environment") val environment: String?,
    @SerializedName("status") val status: TestStatus?,
    @SerializedName("is_finished") val isFinished: Boolean?,
    @SerializedName("prepared_data") val preparedData: List<PreparedData>?,
    @SerializedName("kind") val kind: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("group_list") val groupList: List<String>?,
    @SerializedName("start_time") val startTime: Long?,
    @SerializedName("end_time") val endTime: Long?,
    @SerializedName("attempts_count") val attemptsCount: Int?,
    @SerializedName("success_count") val successCount: Int?,
    @SerializedName("last_error_hash") val lastErrorHash: String?,
    @SerializedName("last_conclusion") val lastConclusion: ConclusionStatus?
) {
    val className: String
        get() = testName.substringBefore(testNameDelimiter)

    val methodName: String
        get() = testName.substringAfter(testNameDelimiter)
}

// gson не переварит private поле
internal const val testNameDelimiter = "::"
