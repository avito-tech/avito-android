package com.avito.logger

import java.nio.file.Path

public data class GradleLoggerMetadata(
    public override val tag: String,
    public val coordinates: GradleLoggerCoordinates
) : FileHandledLoggerMetadata {

    private val asString by lazy {
        buildString {
            append('[')
            append(tag)
            with(coordinates) {
                append('@')
                append(projectPath)
                if (!taskName.isNullOrBlank()) {
                    append(':')
                    append(taskName)
                }
                append(']')
            }
        }
    }

    override val logFilePath: Path

    init {
        with(coordinates) {
            val dir = Path.of(projectPath)
            logFilePath = if (taskName != null) {
                dir.resolve(Path.of("$projectPath:$taskName.logs"))
            } else {
                dir.resolve(Path.of("$projectPath.logs"))
            }
        }
    }

    override fun asString(): String = asString

    override fun asMap(): Map<String, String> {
        val result = mutableMapOf(
            "tag" to tag,
            "project_path" to coordinates.projectPath
        )

        if (!coordinates.taskName.isNullOrBlank()) {
            result["task_name"] = coordinates.taskName
        }

        return result
    }
}
