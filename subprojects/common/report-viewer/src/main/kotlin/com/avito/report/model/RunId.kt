package com.avito.report.model

/**
 * Universal identifier for test suits
 * @param prefix optional prefix
 * @param commitHash unique SHA identifier of commit
 * @param buildTypeId is used to distinguish runs with the same API on `develop` branch
 */
data class RunId(
    private val prefix: String? = null,
    private val commitHash: String,
    private val buildTypeId: String
) {

    fun value(): String = when {
        prefix.isNullOrBlank() -> "$commitHash$DELIMITER$buildTypeId"
        else -> "$prefix$DELIMITER$commitHash$DELIMITER$buildTypeId"
    }
}

private const val DELIMITER = '.'
