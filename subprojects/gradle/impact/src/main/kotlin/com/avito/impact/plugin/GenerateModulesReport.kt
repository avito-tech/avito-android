package com.avito.impact.plugin

import com.avito.impact.ModifiedProjectsFinder
import com.avito.module.configurations.ConfigurationType
import com.avito.module.configurations.ConfigurationType.AndroidTests
import com.avito.module.configurations.ConfigurationType.Main
import com.avito.module.configurations.ConfigurationType.UnitTests
import com.avito.utils.createOrClear
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Paths

/**
 * Prints impact analysis to files
 * FOR TESTING PURPOSES ONLY (assertion on Gradle outputs)
 */
public abstract class GenerateModulesReport : DefaultTask() {

    private val reportsDirectory = Paths.get(project.rootProject.buildDir.toString(), "reports", "modules").toFile()

    @OutputFile
    public val implementationModulesReportFile: File = File(reportsDirectory, "implementation-modules.txt")

    @OutputFile
    public val unitTestsModulesReportFile: File = File(reportsDirectory, "unit-tests-modules.txt")

    @OutputFile
    public val androidTestsModulesReportFile: File = File(reportsDirectory, "android-tests-modules.txt")

    @TaskAction
    public fun printReport() {
        val modifiedProjectsFinder = ModifiedProjectsFinder.from(project)

        printReport(
            implementationModulesReportFile,
            modifiedProjectsFinder.determineImpact(Main)
        )
        printReport(unitTestsModulesReportFile, modifiedProjectsFinder.determineImpact(UnitTests))
        printReport(
            androidTestsModulesReportFile,
            modifiedProjectsFinder.determineImpact(AndroidTests)
        )
    }

    private fun ModifiedProjectsFinder.determineImpact(configurationType: ConfigurationType): Set<Project> {
        return modifiedProjects(configurationType).map { it.project }.toSet()
    }

    private fun printReport(
        report: File,
        modifiedProjects: Set<Project>
    ) {
        report.createOrClear()
        report.printWriter().use { writer ->
            modifiedProjects.forEach { project ->
                writer.println(project.path)
            }
        }
        logger.lifecycle("Wrote changed modules' report file://${report.path}")
    }
}
