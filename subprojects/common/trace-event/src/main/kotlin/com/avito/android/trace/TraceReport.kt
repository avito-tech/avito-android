package com.avito.android.trace

import com.google.gson.annotations.SerializedName

public data class TraceReport(
    @SerializedName("traceEvents") val traceEvents: List<TraceEvent>,
    @SerializedName("metadata") val metadata: Map<String, String>? = null
) {

    public companion object {
        public const val REPORT_SOURCE_METADATA_KEY: String = "reportSource"
        public const val BUILD_FINISHED_REPORT_SOURCE: String = "buildFinished"
        public const val SHUTDOWN_HOOK_REPORT_SOURCE: String = "shutdownHook"
    }
}
