package com.avito.report.model

import com.avito.android.Result

/**
 * Универсальный runId для идентификации сьюта тестов
 * - build config teamcity домешан, чтобы отличать прогон на одном API на develop
 *  (там так же как в релизе нет targetBranch) от buildRelease
 */
data class RunId(val commitHash: String, val buildTypeId: String) {

    override fun toString(): String = "$commitHash$DELIMITER$buildTypeId"

    companion object {

        private const val DELIMITER = '.'

        fun fromString(runId: String): Result<RunId> {
            val split = runId.split(DELIMITER)

            return when (split.size) {
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
