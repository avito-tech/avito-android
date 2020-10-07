package com.avito.android.lint

import com.avito.android.build_verdict.BuildVerdictTask
import com.avito.bitbucket.Bitbucket
import com.avito.bitbucket.Severity
import com.avito.git.gitState
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.getMandatoryStringProperty
import com.avito.utils.logging.ciLogger
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class LintReportTask : DefaultTask(), BuildVerdictTask {

    @get:Input
    abstract val abortOnError: Property<Boolean>

    @get:Input
    abstract val buildId: Property<Int>

    @get:Internal
    abstract val bitbucket: Property<Bitbucket>

    @OutputDirectory
    val reportsDir: File = project.file(project.buildDir.path + "/reports/lint/modules")

    private var _verdict: String? = null

    @get:Internal
    override val verdict: String
        get() = _verdict ?: "Lint has no errors"

    @TaskAction
    fun makeReport() {
        cleanOldReports()
        copyReports()
        val models = parseReports()
        sendToBitbucket(models)
        val report = mergeReports(models)
        failIfNeeded(models, report)
    }

    private fun cleanOldReports() {
        if (reportsDir.exists()) {
            reportsDir.deleteRecursively()
        }
        reportsDir.mkdirs()
    }

    private fun copyReports() {
        project.internalModule
            .implementationConfiguration
            .allDependencies()
            .forEach {
                copyReport(it.module.project)
            }
    }

    private fun copyReport(module: Project) {
        val dir = File(module.buildDir, "reports")
        if (!dir.exists()) return

        val destination = File(
            reportsDir, module.path
                .removePrefix(":")
                .replace(':', '/')
        )
        destination.mkdirs()

        dir.listFiles { file ->
            file.name.startsWith("lint-results")
                && (file.extension == "xml" || file.extension == "html")
        }.forEach { file ->
            file.copyTo(File(destination, file.name))
        }
    }

    private fun parseReports(): List<LintReportModel> = LintResultsParser(reportsDir, project.ciLogger).parse()

    private fun mergeReports(models: List<LintReportModel>): File {
        val report = project.file(project.buildDir.path + "/reports/lint/lint-merge.html")
        LintReportMerger(models, report).write()

        project.ciLogger.debug("Wrote HTML report to file://${report.canonicalPath}")
        return report
    }

    private fun sendToBitbucket(reports: List<LintReportModel>) {
        val git = project.gitState()

        if (!git.isPresent || !bitbucket.isPresent) {
            project.ciLogger.info("Sending to bitbucket skipped")
        } else {
            val gitState = git.get()
            val targetBranch = gitState.targetBranch

            if (targetBranch == null) {
                project.ciLogger.info("Sending to bitbucket skipped: targetBranch not specified")
            } else {
                bitbucket.get()
                    .addInsights(
                        rootDir = project.rootDir,
                        sourceCommitHash = gitState.originalBranch.commit,
                        targetCommitHash = targetBranch.commit,
                        key = "android-lint",
                        title = "Android Lint",
                        link = mergedReportLink(),
                        issues = reports
                            .filterIsInstance<LintReportModel.Valid>()
                            .flatMap { it.issues }
                            .map { issue ->
                                Bitbucket.InsightIssue(
                                    message = issue.message,
                                    path = issue.path,
                                    line = issue.line,
                                    severity = when (issue.severity) {
                                        LintIssue.Severity.UNKNOWN -> Severity.LOW
                                        LintIssue.Severity.WARNING -> Severity.MEDIUM
                                        LintIssue.Severity.ERROR -> Severity.HIGH
                                    }
                                )
                            }
                    )
                    .onFailure { project.ciLogger.critical("Can't create lint report", it) }
            }
        }
    }

    private fun mergedReportLink(): HttpUrl {
        val teamcityUrl = project.getMandatoryStringProperty("teamcityUrl").removeSuffix("/")
        return "${teamcityUrl}/repository/download/AvitoAndroid_Build/${buildId.get()}:id/${project.name}/build/reports/lint-results-release.html".toHttpUrl()
    }

    private fun failIfNeeded(reports: List<LintReportModel>, report: File) {
        if (!abortOnError.getOrElse(true)) return
        val failed = reports.filter { it.hasErrors() }
        if (failed.isEmpty()) return

        val modulePath = failed.joinToString {
            it.projectRelativePath.removePrefix("/")
                .replace('/', ':')
        }
        val verdict = "Lint found errors in the projects $modulePath; aborting build. \n" +
            "See full report: ${report.path}"
        _verdict = verdict
        throw GradleException(
            verdict
        )
    }
}
