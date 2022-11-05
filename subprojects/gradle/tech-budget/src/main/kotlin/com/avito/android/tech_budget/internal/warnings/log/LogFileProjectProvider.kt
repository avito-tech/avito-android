package com.avito.android.tech_budget.internal.warnings.log

import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import java.io.File

internal class LogFileProjectProvider(
    private val rootOutputDir: File,
    private val projectInfo: ProjectInfo,
    private val taskName: String,
    private val projectInfoConverter: ProjectInfoConverter
) {

    private val logFile: File by lazy {
        val taskName = taskName
        val projectOutputDir = File(rootOutputDir, encodePath(projectInfo.path))
        val projectNameFile = File(projectOutputDir, PROJECT_INFO_FILE)
        if (!projectNameFile.exists()) {
            projectOutputDir.mkdirs()
            projectNameFile.createNewFile()
            val projectInfoRaw = projectInfoConverter.convertToString(projectInfo)
            projectNameFile.writeText(projectInfoRaw)
        }
        val file = File(projectOutputDir, "$taskName.log")
        if (!file.exists()) {
            file.createNewFile()
        }
        file
    }

    fun provideLogFile(): File {
        return logFile
    }

    /**
     * Encodes project path to make it suitable for being a directory
     * Format: :avito-app:serp -> avito-app_serp
     */
    private fun encodePath(projectPath: String) =
        projectPath
            .drop(1)
            .replace(":", "_")

    companion object {
        const val PROJECT_INFO_FILE = ".project"
    }
}
