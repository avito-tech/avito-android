package com.avito.report.model

/**
 * Universal identifier for test suits
 * @param prefix optional prefix
 * @param identifier unique identifier,
 *          should be the same if source not changed, so commit hash should be used instead of buildId
 *          local builds could use current time, to avoid git interaction
 * @param buildTypeId is used to distinguish runs with the same API on `develop` branch
 */
public data class RunId(
    private val prefix: String? = null,
    private val identifier: String,
    private val buildTypeId: String
) {

    public fun toReportViewerFormat(): String = when {
        prefix.isNullOrBlank() -> "$identifier$DELIMITER$buildTypeId"
        else -> "$prefix$DELIMITER$identifier$DELIMITER$buildTypeId"
    }

    public companion object {
        internal const val DELIMITER = '.'
    }
}
