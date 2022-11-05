package com.avito.android.tech_budget.internal.warnings.log

import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter.Companion.DEFAULT_SEPARATOR
import com.avito.android.tech_budget.internal.warnings.log.LogFileProjectProvider.Companion.PROJECT_INFO_FILE
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import java.io.File

internal class FileLogReader(
    private val rootOutputDir: File,
    private val separator: String = DEFAULT_SEPARATOR,
    private val projectInfoConverter: ProjectInfoConverter,
) : LogReader {

    override fun getAll(): List<LogEntry> {
        val outputDir = rootOutputDir
        if (!outputDir.exists()) return emptyList()
        val projects = outputDir.listFiles() ?: emptyArray()
        return projects.flatMap { collectProjectLogs(it) }
    }

    private fun collectProjectLogs(projectDir: File): List<LogEntry> {
        val projectFiles = projectDir.listFiles()
            ?: error("Project must be a directory and must contain files with logs. Directory: $projectDir")
        val projectInfoFile = projectFiles.find { it.name == PROJECT_INFO_FILE }
            ?: error("File `.project` must present in project directory with warnings report. Directory: $projectDir")
        val projectInfo = readProjectFile(projectInfoFile)
        val logFiles = projectFiles.filter { it.name != PROJECT_INFO_FILE }
        return logFiles.flatMap { collectTaskLogs(it, projectInfo) }
    }

    private fun readProjectFile(projectMetaFile: File): ProjectInfo {
        val rawInfo = projectMetaFile.readText()
        val projectInfo = projectInfoConverter.extractFromString(rawInfo)

        check(projectInfo.path.isNotBlank()) { "Project path can't be blank. File: $projectMetaFile" }
        return projectInfo
    }

    private fun collectTaskLogs(
        taskLogFile: File,
        projectInfo: ProjectInfo
    ): List<LogEntry> {
        var lastLog = ""
        val logs = mutableListOf<LogEntry>()
        val taskName = taskLogFile.name

        taskLogFile.forEachLine { line ->
            lastLog += line + '\n'
            if (line.endsWith(separator)) {
                logs.add(LogEntry(projectInfo, taskName, lastLog.removeSuffix("$separator\n")))
                lastLog = ""
            }
        }

        return logs
    }
}
