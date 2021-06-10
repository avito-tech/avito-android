package com.avito.report.model

public data class Incident(
    val type: Type,
    val timestamp: Long,
    val trace: List<String>,
    val chain: List<IncidentElement>,
    val entryList: List<Entry>
) {

    public val errorMessage: String = chain.firstOrNull()?.message ?: "Unknown"

    public enum class Type {

        /**
         * abnormal test execution
         */
        INFRASTRUCTURE_ERROR,

        /**
         * assertions not being fulfilled
         */
        ASSERTION_FAILED
    }

    // for test fixtures
    public companion object
}
