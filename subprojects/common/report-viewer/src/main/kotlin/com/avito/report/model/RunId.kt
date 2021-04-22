package com.avito.report.model

import com.avito.android.Result

/**
 * Universal identifier for test suits
 * @param prefix optional prefix
 * @param commitHash unique SHA identifier of commit
 * @param buildTypeId is used to distinguish runs with the same API on `develop` branch
 */
data class RunId(
    val prefix: String = "",
    val commitHash: String,
    val buildTypeId: String
) {

    init {
        require(!prefix.contains(DELIMITER)) { "prefix could not contain '$DELIMITER' character" }
        require(!commitHash.contains(DELIMITER)) { "commitHash could not contain '$DELIMITER' character" }
        require(!buildTypeId.contains(DELIMITER)) { "buildTypeId could not contain '$DELIMITER' character" }
    }

    override fun toString(): String = when {
        prefix.isBlank() -> "$commitHash$DELIMITER$buildTypeId"
        else -> "$prefix$DELIMITER$commitHash$DELIMITER$buildTypeId"
    }

    companion object {

        const val DELIMITER = ':'

        fun fromString(runId: String): Result<RunId> {
            val split = runId.split(DELIMITER)

            return when (split.size) {
                3 -> Result.Success(
                    RunId(
                        prefix = split.component1(),
                        commitHash = split.component2(),
                        buildTypeId = split.component3()
                    )
                )
                2 -> Result.Success(
                    RunId(
                        commitHash = split.component1(),
                        buildTypeId = split.component2()
                    )
                )
                else -> Result.Failure(IllegalArgumentException("Invalid runId: $runId"))
            }
        }
    }
}
