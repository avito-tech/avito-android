package com.avito.report.model

import com.google.gson.annotations.SerializedName

public data class Incident(
    @SerializedName("type") val type: Type,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("trace") val trace: List<String>,
    @SerializedName("chain") val chain: List<IncidentElement>,
    @SerializedName("entry_list") val entryList: List<Entry>
) {

    public enum class Type {

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
    public companion object
}
