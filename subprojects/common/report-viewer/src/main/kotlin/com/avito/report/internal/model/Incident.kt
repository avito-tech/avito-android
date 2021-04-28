package com.avito.report.internal.model

import com.avito.report.model.Entry
import com.google.gson.annotations.SerializedName

internal data class Incident(
    @SerializedName("type") val type: Type,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("trace") val trace: List<String>,
    @SerializedName("chain") val chain: List<IncidentElement>,
    @SerializedName("entry_list") val entryList: List<Entry>
) {

    enum class Type {

        /**
         * abnormal test execution
         */
        @SerializedName("error")
        INFRASTRUCTURE_ERROR,

        /**
         * assertions not being fulfilled
         */
        @SerializedName("failure")
        ASSERTION_FAILED
    }

    // for test fixtures
    companion object
}
