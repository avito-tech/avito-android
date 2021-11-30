package com.avito.logger

import java.nio.file.Path

internal class GradleLoggerMetadata(
    private val tag: String,
    private val coordinates: GradleLoggerCoordinates
) : FileHandledLoggerMetadata {

    override val asMessagePrefix: String by lazy {
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
            val windowsCompatible = replaceWindowsPathIncompatibleChars(projectPath)
            val dir = Path.of(windowsCompatible)
            logFilePath = if (taskName != null) {
                dir.resolve(Path.of("$taskName.logs"))
            } else {
                dir.resolve(Path.of("$windowsCompatible.logs"))
            }
        }
    }

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

    private companion object {

        /**
         * ':' is illegal for using in Windows files paths
         */
        fun replaceWindowsPathIncompatibleChars(projectPath: String): String {
            return when (projectPath) {
                ":" -> "root"
                else -> projectPath.removePrefix(":").replace(':', '_')
            }
        }
    }
}
